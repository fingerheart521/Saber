/**
 * Copyright (c) 2018-2099, Chill Zhuang 庄骞 (bladejava@qq.com).
 * Modifications Copyright (c) 2026, fingerheart521 (daoguangliu@qq.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springblade.flow.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.MultiInstanceLoopCharacteristics;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.secure.BladeUser;
import org.springblade.core.secure.utils.SecureUtil;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.flow.dto.CompleteTaskDTO;
import org.springblade.flow.dto.ProcessFormFieldDTO;
import org.springblade.flow.dto.TaskActionDTO;
import org.springblade.flow.service.FlowDesignConfigService;
import org.springblade.flow.service.IProcessFormService;
import org.springblade.flow.service.IProcessTaskService;
import org.springblade.flow.vo.ProcessNodeVO;
import org.springblade.flow.vo.ProcessFormVO;
import org.springblade.flow.vo.ProcessRuntimeFormVO;
import org.springblade.flow.vo.ProcessTaskVO;
import org.springblade.system.user.entity.UserInfo;
import org.springblade.system.user.feign.IUserClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 流程任务服务实现类
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class ProcessTaskServiceImpl implements IProcessTaskService {

	private static final int DEFAULT_PAGE_NUMBER = 1;
	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final int MAX_PAGE_SIZE = 100;
	private static final String RETURN_FROM_ACTIVITY = "_bladeReturnFromActivity";
	private static final String RETURN_TO_ACTIVITY = "_bladeReturnToActivity";
	private static final Set<String> SUPPORTED_ACTIONS = Set.of(
		"wf_pass", "wf_reject", "wf_transfer", "wf_delegate", "wf_terminate",
		"wf_add_instance", "wf_del_instance", "wf_rollback", "wf_draft", "wf_withdraw"
	);

	private final TaskService taskService;
	private final HistoryService historyService;
	private final RuntimeService runtimeService;
	private final RepositoryService repositoryService;
	private final FlowDesignConfigService flowDesignConfigService;
	private final IProcessFormService processFormService;
	private final IUserClient userClient;

	@Override
	public IPage<ProcessTaskVO> todo(Integer current, Integer size) {
		PageRequest pageRequest = resolvePage(current, size);
		TaskQuery query = candidateOrAssignedQuery(taskService.createTaskQuery()
			.taskTenantId(currentTenantId()));
		long total = query.count();
		List<ProcessTaskVO> records = query.orderByTaskCreateTime().desc()
			.listPage(pageRequest.firstResult(), pageRequest.pageSize())
			.stream()
			.map(this::buildTaskVO)
			.toList();
		Page<ProcessTaskVO> page = new Page<>(pageRequest.pageNumber(), pageRequest.pageSize(), total);
		page.setRecords(records);
		return page;
	}

	@Override
	public IPage<ProcessTaskVO> done(Integer current, Integer size) {
		PageRequest pageRequest = resolvePage(current, size);
		HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
			.taskTenantId(currentTenantId())
			.taskAssignee(currentUserId())
			.finished();
		long total = query.count();
		List<ProcessTaskVO> records = query.orderByHistoricTaskInstanceEndTime().desc()
			.listPage(pageRequest.firstResult(), pageRequest.pageSize())
			.stream()
			.map(this::buildHistoricTaskVO)
			.toList();
		Page<ProcessTaskVO> page = new Page<>(pageRequest.pageNumber(), pageRequest.pageSize(), total);
		page.setRecords(records);
		return page;
	}

	@Override
	public ProcessTaskVO current(String processInstanceId) {
		if (!StringUtils.hasText(processInstanceId)) {
			throw new ServiceException("流程实例ID不能为空");
		}
		Task task = taskService.createTaskQuery()
			.processInstanceId(processInstanceId.trim())
			.taskTenantId(currentTenantId())
			.singleResult();
		if (task == null) {
			return null;
		}
		try {
			assertTaskReadable(task);
		} catch (ServiceException exception) {
			return null;
		}
		return buildTaskVO(task);
	}

	@Override
	public ProcessRuntimeFormVO form(String taskId) {
		Task task = getTenantTask(taskId);
		assertTaskReadable(task);
		String formType = flowDesignConfigService.processProperty(task.getProcessDefinitionId(), "formType");
		String formKey = resolveFormKey(task);
		ProcessRuntimeFormVO runtimeForm = new ProcessRuntimeFormVO();
		runtimeForm.setFormType(StringUtils.hasText(formType) ? formType : "internal");
		runtimeForm.setFormKey(formKey);
		runtimeForm.setFields(resolveFormFields(task));
		runtimeForm.setValues(new LinkedHashMap<>(runtimeService.getVariables(task.getProcessInstanceId())));
		if (!StringUtils.hasText(formKey) || "external".equals(runtimeForm.getFormType())) {
			return runtimeForm;
		}
		ProcessFormVO processForm = processFormService.detailByKey(formKey);
		runtimeForm.setFormName(processForm.getName());
		runtimeForm.setFormJson(processForm.getFormJson());
		return runtimeForm;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean claim(String taskId) {
		Task task = getTenantTask(taskId);
		String userId = currentUserId();
		if (userId.equals(task.getAssignee())) {
			return true;
		}
		if (StringUtils.hasText(task.getAssignee())) {
			throw new ServiceException("任务已被其他用户签收");
		}
		assertCandidate(taskId, userId);
		taskService.claim(taskId, userId);
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean unclaim(String taskId) {
		Task task = getTenantTask(taskId);
		if (!currentUserId().equals(task.getAssignee())) {
			throw new ServiceException("只能取消签收本人办理的任务");
		}
		taskService.unclaim(taskId);
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean complete(CompleteTaskDTO completeTaskDTO) {
		Task task = getTenantTask(completeTaskDTO.getTaskId());
		String userId = currentUserId();
		if (!StringUtils.hasText(task.getAssignee())) {
			assertCandidate(task.getId(), userId);
			taskService.claim(task.getId(), userId);
		} else if (!userId.equals(task.getAssignee())) {
			throw new ServiceException("当前用户不是该任务的办理人");
		}
		if (StringUtils.hasText(completeTaskDTO.getComment())) {
			taskService.addComment(task.getId(), task.getProcessInstanceId(), completeTaskDTO.getComment().trim());
		}
		Map<String, Object> variables = completeTaskDTO.getVariables() == null
			? new HashMap<>()
			: new HashMap<>(completeTaskDTO.getVariables());
		removeReadOnlyFormVariables(task, variables);
		taskService.complete(task.getId(), variables);
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean action(TaskActionDTO taskActionDTO) {
		String action = taskActionDTO.getAction().trim();
		if (!SUPPORTED_ACTIONS.contains(action)) {
			throw new ServiceException("不支持的任务动作");
		}
		Task task = getTenantTask(taskActionDTO.getTaskId());
		assertActionEnabled(task, action);
		if ("wf_withdraw".equals(action)) {
			withdraw(task, taskActionDTO.getComment());
			return true;
		}
		assertOperator(task);
		addComment(task, taskActionDTO.getComment());
		Map<String, Object> variables = taskActionDTO.getVariables() == null
			? new HashMap<>()
			: new HashMap<>(taskActionDTO.getVariables());
		removeReadOnlyFormVariables(task, variables);
		switch (action) {
			case "wf_pass" -> pass(task, variables);
			case "wf_reject" -> reject(task, variables);
			case "wf_transfer" -> transfer(task, taskActionDTO.getTargetUserId());
			case "wf_delegate" -> delegate(task, taskActionDTO.getTargetUserId());
			case "wf_terminate" -> runtimeService.deleteProcessInstance(task.getProcessInstanceId(),
				StringUtils.hasText(taskActionDTO.getComment()) ? taskActionDTO.getComment().trim() : "流程终止");
			case "wf_add_instance" -> addInstance(task, taskActionDTO.getTargetUserId(), variables);
			case "wf_del_instance" -> deleteInstance(task);
			case "wf_rollback" -> rollback(task, taskActionDTO.getTargetActivityId(), variables);
			case "wf_draft" -> saveDraft(task, taskActionDTO.getComment(), variables);
			default -> throw new ServiceException("不支持的任务动作");
		}
		return true;
	}

	private void assertActionEnabled(Task task, String action) {
		List<String> buttons = flowDesignConfigService.taskButtons(
			task.getProcessDefinitionId(), task.getTaskDefinitionKey());
		if (!buttons.contains(action)) {
			throw new ServiceException("当前节点未启用该操作");
		}
	}

	private void assertOperator(Task task) {
		String userId = currentUserId();
		if (!StringUtils.hasText(task.getAssignee())) {
			assertCandidate(task.getId(), userId);
			taskService.claim(task.getId(), userId);
		} else if (!userId.equals(task.getAssignee())) {
			throw new ServiceException("当前用户不是该任务的办理人");
		}
	}

	private void assertTaskReadable(Task task) {
		String userId = currentUserId();
		if (StringUtils.hasText(task.getAssignee())) {
			if (!userId.equals(task.getAssignee())) {
				throw new ServiceException("当前用户无权查看该任务表单");
			}
			return;
		}
		assertCandidate(task.getId(), userId);
	}

	private String resolveFormKey(Task task) {
		UserTask userTask = getUserTask(task);
		if (StringUtils.hasText(userTask.getFormKey())) {
			return userTask.getFormKey().trim();
		}
		String processFormKey = flowDesignConfigService.processProperty(task.getProcessDefinitionId(), "formKey");
		if (StringUtils.hasText(processFormKey)) {
			return processFormKey.trim();
		}
		return repositoryService.getBpmnModel(task.getProcessDefinitionId())
			.getMainProcess()
			.findFlowElementsOfType(StartEvent.class)
			.stream()
			.map(StartEvent::getFormKey)
			.filter(StringUtils::hasText)
			.findFirst()
			.map(String::trim)
			.orElse(null);
	}

	private List<ProcessFormFieldDTO> resolveFormFields(Task task) {
		String fieldsJson = flowDesignConfigService.elementProperty(
			task.getProcessDefinitionId(), task.getTaskDefinitionKey(), "formPermissions");
		if (!StringUtils.hasText(fieldsJson)) {
			fieldsJson = flowDesignConfigService.processProperty(task.getProcessDefinitionId(), "formFields");
		}
		if (!StringUtils.hasText(fieldsJson)) {
			return Collections.emptyList();
		}
		try {
			ProcessFormFieldDTO[] fields = JsonUtil.parse(fieldsJson, ProcessFormFieldDTO[].class);
			return fields == null ? Collections.emptyList() : Arrays.asList(fields);
		} catch (Exception exception) {
			throw new ServiceException("流程表单字段权限配置无法解析");
		}
	}

	private void removeReadOnlyFormVariables(Task task, Map<String, Object> variables) {
		resolveFormFields(task).stream()
			.filter(field -> StringUtils.hasText(field.getProp()) && !Boolean.TRUE.equals(field.getWritable()))
			.map(ProcessFormFieldDTO::getProp)
			.forEach(variables::remove);
	}

	private void addComment(Task task, String comment) {
		if (StringUtils.hasText(comment)) {
			taskService.addComment(task.getId(), task.getProcessInstanceId(), comment.trim());
		}
	}

	private void pass(Task task, Map<String, Object> variables) {
		String returnFrom = stringVariable(task.getProcessInstanceId(), RETURN_FROM_ACTIVITY);
		String returnTo = stringVariable(task.getProcessInstanceId(), RETURN_TO_ACTIVITY);
		if (task.getTaskDefinitionKey().equals(returnFrom) && StringUtils.hasText(returnTo)) {
			runtimeService.createChangeActivityStateBuilder()
				.processInstanceId(task.getProcessInstanceId())
				.processVariables(variables)
				.moveActivityIdTo(task.getTaskDefinitionKey(), returnTo)
				.changeState();
			runtimeService.removeVariables(task.getProcessInstanceId(), List.of(RETURN_FROM_ACTIVITY, RETURN_TO_ACTIVITY));
			return;
		}
		if (DelegationState.PENDING.equals(task.getDelegationState())) {
			taskService.resolveTask(task.getId(), variables);
		} else {
			taskService.complete(task.getId(), variables);
		}
	}

	private void reject(Task task, Map<String, Object> variables) {
		String target = flowDesignConfigService.elementProperty(
			task.getProcessDefinitionId(), task.getTaskDefinitionKey(), "rejectTarget");
		if (!StringUtils.hasText(target)) {
			target = flowDesignConfigService.processProperty(
				task.getProcessDefinitionId(), "defaultRejectTarget");
		}
		if (!StringUtils.hasText(target)) {
			target = previousTargets(task).stream().findFirst().map(ProcessNodeVO::getId).orElse(null);
		}
		target = normalizeTarget(task.getProcessDefinitionId(), target);
		if (!StringUtils.hasText(target) || target.equals(task.getTaskDefinitionKey())) {
			throw new ServiceException("没有可退回的目标节点");
		}
		boolean returnToRejector = "true".equals(flowDesignConfigService.elementProperty(
			task.getProcessDefinitionId(), task.getTaskDefinitionKey(), "returnToRejector"));
		if (returnToRejector) {
			variables.put(RETURN_FROM_ACTIVITY, target);
			variables.put(RETURN_TO_ACTIVITY, task.getTaskDefinitionKey());
		}
		moveActivity(task, target, variables);
	}

	private void rollback(Task task, String targetActivityId, Map<String, Object> variables) {
		if (!StringUtils.hasText(targetActivityId)) {
			throw new ServiceException("请选择回退节点");
		}
		String target = targetActivityId.trim();
		boolean allowed = previousTargets(task).stream().anyMatch(node -> target.equals(node.getId()));
		if (!allowed) {
			throw new ServiceException("只能回退到本流程已完成的用户任务节点");
		}
		moveActivity(task, target, variables);
	}

	private void moveActivity(Task task, String target, Map<String, Object> variables) {
		runtimeService.createChangeActivityStateBuilder()
			.processInstanceId(task.getProcessInstanceId())
			.processVariables(variables)
			.moveActivityIdTo(task.getTaskDefinitionKey(), target)
			.changeState();
	}

	private void transfer(Task task, String targetUserId) {
		if (!StringUtils.hasText(targetUserId)) {
			throw new ServiceException("请选择转办用户");
		}
		taskService.setOwner(task.getId(), currentUserId());
		taskService.setAssignee(task.getId(), targetUserId.trim());
	}

	private void delegate(Task task, String targetUserId) {
		if (!StringUtils.hasText(targetUserId)) {
			throw new ServiceException("请选择委托用户");
		}
		taskService.delegateTask(task.getId(), targetUserId.trim());
	}

	private void addInstance(Task task, String targetUserId, Map<String, Object> variables) {
		if (!StringUtils.hasText(targetUserId)) {
			throw new ServiceException("请选择加签用户");
		}
		UserTask userTask = getUserTask(task);
		MultiInstanceLoopCharacteristics loop = userTask.getLoopCharacteristics();
		if (loop == null) {
			throw new ServiceException("当前节点不是多实例任务，不能加签");
		}
		Execution execution = runtimeService.createExecutionQuery().executionId(task.getExecutionId()).singleResult();
		if (execution == null || !StringUtils.hasText(execution.getParentId())) {
			throw new ServiceException("无法定位多实例执行实例");
		}
		Map<String, Object> instanceVariables = new HashMap<>(variables);
		String elementVariable = loop.getElementVariable();
		if (StringUtils.hasText(elementVariable)) {
			instanceVariables.put(elementVariable, targetUserId.trim());
		}
		runtimeService.addMultiInstanceExecution(
			task.getTaskDefinitionKey(), execution.getParentId(), instanceVariables);
	}

	private void deleteInstance(Task task) {
		if (getUserTask(task).getLoopCharacteristics() == null) {
			throw new ServiceException("当前节点不是多实例任务，不能减签");
		}
		runtimeService.deleteMultiInstanceExecution(task.getExecutionId(), false);
	}

	private void saveDraft(Task task, String comment, Map<String, Object> variables) {
		Map<String, Object> draftVariables = new HashMap<>(variables);
		draftVariables.put("_bladeDraftUser", currentUserId());
		draftVariables.put("_bladeDraftComment", StringUtils.hasText(comment) ? comment.trim() : "");
		taskService.setVariablesLocal(task.getId(), draftVariables);
	}

	private void withdraw(Task task, String comment) {
		HistoricProcessInstance process = historyService.createHistoricProcessInstanceQuery()
			.processInstanceId(task.getProcessInstanceId())
			.processInstanceTenantId(currentTenantId())
			.singleResult();
		if (process == null || (!currentUserId().equals(process.getStartUserId())
			&& !SecureUtil.isAdmin() && !SecureUtil.isAdministrator())) {
			throw new ServiceException("仅流程发起人或管理员可以撤销流程");
		}
		runtimeService.deleteProcessInstance(task.getProcessInstanceId(),
			StringUtils.hasText(comment) ? comment.trim() : "发起人撤销流程");
	}

	private String stringVariable(String processInstanceId, String name) {
		Object value = runtimeService.getVariable(processInstanceId, name);
		return value == null ? null : String.valueOf(value);
	}

	private UserTask getUserTask(Task task) {
		FlowElement flowElement = repositoryService.getBpmnModel(task.getProcessDefinitionId())
			.getMainProcess().getFlowElement(task.getTaskDefinitionKey(), true);
		if (!(flowElement instanceof UserTask userTask)) {
			throw new ServiceException("无法读取当前用户任务定义");
		}
		return userTask;
	}

	private String normalizeTarget(String processDefinitionId, String target) {
		BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
		if (StringUtils.hasText(target)) {
			FlowElement element = bpmnModel.getMainProcess().getFlowElement(target, true);
			if (element instanceof UserTask) {
				return target;
			}
			throw new ServiceException("配置的驳回节点不存在或不是用户任务");
		}
		return findFirstUserTask(bpmnModel);
	}

	private String findFirstUserTask(BpmnModel bpmnModel) {
		StartEvent startEvent = bpmnModel.getMainProcess().findFlowElementsOfType(StartEvent.class)
			.stream().findFirst().orElse(null);
		if (startEvent == null) {
			return null;
		}
		Deque<FlowNode> queue = new ArrayDeque<>();
		Set<String> visited = new LinkedHashSet<>();
		queue.add(startEvent);
		while (!queue.isEmpty()) {
			FlowNode node = queue.removeFirst();
			if (!visited.add(node.getId())) {
				continue;
			}
			if (node instanceof UserTask) {
				return node.getId();
			}
			for (SequenceFlow flow : node.getOutgoingFlows()) {
				if (flow.getTargetFlowElement() instanceof FlowNode targetNode) {
					queue.addLast(targetNode);
				}
			}
		}
		return null;
	}

	private Task getTenantTask(String taskId) {
		if (!StringUtils.hasText(taskId)) {
			throw new ServiceException("任务ID不能为空");
		}
		Task task = taskService.createTaskQuery()
			.taskId(taskId)
			.taskTenantId(currentTenantId())
			.singleResult();
		if (task == null) {
			throw new ServiceException("任务不存在、已处理或无权访问");
		}
		return task;
	}

	private void assertCandidate(String taskId, String userId) {
		TaskQuery query = taskService.createTaskQuery()
			.taskId(taskId)
			.taskTenantId(currentTenantId())
			.or()
			.taskCandidateUser(userId);
		List<String> candidateGroups = currentCandidateGroups();
		if (!candidateGroups.isEmpty()) {
			query.taskCandidateGroupIn(candidateGroups);
		}
		Task candidateTask = query.endOr().singleResult();
		if (candidateTask == null) {
			throw new ServiceException("当前用户不是该任务的候选办理人");
		}
	}

	private TaskQuery candidateOrAssignedQuery(TaskQuery query) {
		TaskQuery candidateQuery = query.or().taskCandidateOrAssigned(currentUserId());
		List<String> candidateGroups = currentCandidateGroups();
		if (!candidateGroups.isEmpty()) {
			candidateQuery.taskCandidateGroupIn(candidateGroups);
		}
		return candidateQuery.endOr();
	}

	private List<String> currentCandidateGroups() {
		BladeUser bladeUser = SecureUtil.getUser();
		if (bladeUser == null) {
			return new ArrayList<>();
		}
		Set<String> candidateGroups = new LinkedHashSet<>();
		addCandidateGroups(candidateGroups, bladeUser.getRoleId(), "role");
		addCandidateGroups(candidateGroups, bladeUser.getRoleName(), "role");
		addCandidateGroups(candidateGroups, bladeUser.getDeptId(), "dept");
		try {
			R<UserInfo> userInfo = userClient.userInfo(SecureUtil.getUserId());
			if (userInfo.isSuccess() && userInfo.getData() != null && userInfo.getData().getUser() != null) {
				addCandidateGroups(candidateGroups, userInfo.getData().getUser().getPostId(), "post");
			}
		} catch (Exception ignored) {
			// 岗位信息仅作补充，查询失败时角色和部门候选组仍可正常匹配。
		}
		return new ArrayList<>(candidateGroups);
	}

	private void addCandidateGroups(Set<String> candidateGroups, String values, String prefix) {
		if (!StringUtils.hasText(values)) {
			return;
		}
		Arrays.stream(values.split(","))
			.map(String::trim)
			.filter(StringUtils::hasText)
			.forEach(value -> {
				candidateGroups.add(value);
				candidateGroups.add(prefix + ":" + value);
			});
	}

	private ProcessTaskVO buildTaskVO(Task task) {
		ProcessTaskVO vo = new ProcessTaskVO();
		vo.setId(task.getId());
		vo.setName(task.getName());
		vo.setTaskDefinitionKey(task.getTaskDefinitionKey());
		vo.setProcessInstanceId(task.getProcessInstanceId());
		vo.setProcessDefinitionId(task.getProcessDefinitionId());
		vo.setBusinessKey(getBusinessKey(task.getProcessInstanceId()));
		vo.setAssignee(task.getAssignee());
		vo.setCreateTime(task.getCreateTime());
		vo.setDueDate(task.getDueDate());
		vo.setTenantId(task.getTenantId());
		vo.setAvailableButtons(flowDesignConfigService.taskButtons(
			task.getProcessDefinitionId(), task.getTaskDefinitionKey()));
		vo.setRollbackTargets(previousTargets(task));
		vo.setNodeProperties(new LinkedHashMap<>(flowDesignConfigService.getConfig(task.getProcessDefinitionId())
			.getElementProperties().getOrDefault(task.getTaskDefinitionKey(), Collections.emptyMap())));
		return vo;
	}

	private ProcessTaskVO buildHistoricTaskVO(HistoricTaskInstance task) {
		ProcessTaskVO vo = new ProcessTaskVO();
		vo.setId(task.getId());
		vo.setName(task.getName());
		vo.setTaskDefinitionKey(task.getTaskDefinitionKey());
		vo.setProcessInstanceId(task.getProcessInstanceId());
		vo.setProcessDefinitionId(task.getProcessDefinitionId());
		vo.setBusinessKey(getBusinessKey(task.getProcessInstanceId()));
		vo.setAssignee(task.getAssignee());
		vo.setCreateTime(task.getCreateTime());
		vo.setEndTime(task.getEndTime());
		vo.setDueDate(task.getDueDate());
		vo.setTenantId(task.getTenantId());
		return vo;
	}

	private String getBusinessKey(String processInstanceId) {
		HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery()
			.processInstanceId(processInstanceId)
			.processInstanceTenantId(currentTenantId())
			.singleResult();
		return processInstance == null ? null : processInstance.getBusinessKey();
	}

	private List<ProcessNodeVO> previousTargets(Task task) {
		Set<String> seen = new LinkedHashSet<>();
		return historyService.createHistoricTaskInstanceQuery()
			.processInstanceId(task.getProcessInstanceId())
			.finished()
			.orderByHistoricTaskInstanceEndTime().desc()
			.list()
			.stream()
			.filter(history -> StringUtils.hasText(history.getTaskDefinitionKey()))
			.filter(history -> seen.add(history.getTaskDefinitionKey()))
			.map(history -> new ProcessNodeVO(
				history.getTaskDefinitionKey(),
				StringUtils.hasText(history.getName()) ? history.getName() : history.getTaskDefinitionKey()))
			.toList();
	}

	private PageRequest resolvePage(Integer current, Integer size) {
		int pageNumber = current == null || current < DEFAULT_PAGE_NUMBER ? DEFAULT_PAGE_NUMBER : current;
		int pageSize = size == null || size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
		long firstResult = (long) (pageNumber - 1) * pageSize;
		if (firstResult > Integer.MAX_VALUE) {
			throw new ServiceException("分页参数超出允许范围");
		}
		return new PageRequest(pageNumber, pageSize, (int) firstResult);
	}

	private String currentTenantId() {
		String tenantId = SecureUtil.getTenantId();
		if (!StringUtils.hasText(tenantId)) {
			throw new ServiceException("无法获取当前租户信息");
		}
		return tenantId;
	}

	private String currentUserId() {
		Long userId = SecureUtil.getUserId();
		if (userId == null || userId <= 0) {
			throw new ServiceException("无法获取当前用户信息");
		}
		return String.valueOf(userId);
	}

	private record PageRequest(int pageNumber, int pageSize, int firstResult) {
	}

}
