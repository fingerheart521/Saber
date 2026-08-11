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
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.task.Comment;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.redis.cache.BladeRedis;
import org.springblade.core.secure.utils.SecureUtil;
import org.springblade.flow.dto.StartProcessDTO;
import org.springblade.flow.service.FlowDesignConfigService;
import org.springblade.flow.service.IProcessInstanceService;
import org.springblade.flow.vo.ProcessHistoryVO;
import org.springblade.flow.vo.ProcessInstanceVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 流程实例服务实现类
 *
 * @author Chill
 */
@Service
@AllArgsConstructor
public class ProcessInstanceServiceImpl implements IProcessInstanceService {

	private static final String PROCUREMENT_REQUIREMENT_APPROVAL = "procurementRequirementApproval";
	private static final String PROCUREMENT_REQUIREMENT_APPROVAL_RESOURCE =
		"processes/procurement-requirement-approval.bpmn20.xml";
	private static final String PROCUREMENT_PURCHASE_REQUIREMENT_APPROVAL =
		"procurementPurchaseRequirementApproval";
	private static final String PROCUREMENT_PURCHASE_REQUIREMENT_APPROVAL_RESOURCE =
		"processes/procurement-purchase-requirement-approval.bpmn20.xml";
	private static final String PROCUREMENT_BIDDING_REQUIREMENT_APPROVAL =
		"procurementBiddingRequirementApproval";
	private static final String PROCUREMENT_BIDDING_REQUIREMENT_APPROVAL_RESOURCE =
		"processes/procurement-bidding-requirement-approval.bpmn20.xml";
	private static final String PROCUREMENT_REVIEW_EXPERT_ADMISSION_APPROVAL =
		"procurementReviewExpertAdmissionApproval";
	private static final String PROCUREMENT_REVIEW_EXPERT_ADMISSION_APPROVAL_RESOURCE =
		"processes/procurement-review-expert-admission-approval.bpmn20.xml";
	private static final String PROCUREMENT_REVIEW_EXPERT_RETIREMENT_APPROVAL =
		"procurementReviewExpertRetirementApproval";
	private static final String PROCUREMENT_REVIEW_EXPERT_RETIREMENT_APPROVAL_RESOURCE =
		"processes/procurement-review-expert-retirement-approval.bpmn20.xml";
	private static final int DEFAULT_PAGE_NUMBER = 1;
	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final int MAX_PAGE_SIZE = 100;
	private static final String DEFAULT_CANCEL_REASON = "发起人撤销流程";
	private static final String SERIAL_VARIABLE = "serialNumber";
	private static final Pattern TITLE_TOKEN_PATTERN = Pattern.compile("\\$\\{#([^}]+)}");
	private static final Pattern DATE_FUNCTION_PATTERN = Pattern.compile("dateFormat\\('([^']+)'\\)");

	private final RepositoryService repositoryService;
	private final RuntimeService runtimeService;
	private final HistoryService historyService;
	private final IdentityService identityService;
	private final TaskService taskService;
	private final FlowDesignConfigService flowDesignConfigService;
	private final BladeRedis bladeRedis;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessInstanceVO start(StartProcessDTO startProcessDTO) {
		if (startProcessDTO == null
			|| !StringUtils.hasText(startProcessDTO.getProcessDefinitionKey())
			|| !StringUtils.hasText(startProcessDTO.getBusinessKey())) {
			throw new ServiceException("流程定义标识和业务标识不能为空");
		}
		String tenantId = currentTenantId();
		String userId = currentUserId();
		String definitionKey = startProcessDTO.getProcessDefinitionKey().trim();
		String businessKey = startProcessDTO.getBusinessKey().trim();
		ensureBuiltInDefinition(definitionKey, tenantId);

		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
			.processDefinitionTenantId(tenantId)
			.processDefinitionKey(definitionKey)
			.active()
			.latestVersion()
			.singleResult();
		if (processDefinition == null) {
			throw new ServiceException("流程定义不存在、已挂起或无权访问");
		}

		long existingCount = historyService.createHistoricProcessInstanceQuery()
			.processInstanceTenantId(tenantId)
			.processInstanceBusinessKey(businessKey)
			.count();
		if (existingCount > 0) {
			throw new ServiceException("该业务已发起流程，请勿重复提交");
		}

		Map<String, Object> variables = startProcessDTO.getVariables() == null
			? new HashMap<>()
			: new HashMap<>(startProcessDTO.getVariables());
		variables.put("initiator", userId);
		String serialNumber = generateSerialNumber(processDefinition, tenantId);
		if (StringUtils.hasText(serialNumber)) {
			variables.put(SERIAL_VARIABLE, serialNumber);
		}
		String title = resolveProcessTitle(processDefinition, variables, userId);

		identityService.setAuthenticatedUserId(userId);
		try {
			ProcessInstance processInstance = runtimeService.createProcessInstanceBuilder()
				.processDefinitionId(processDefinition.getId())
				.businessKey(businessKey)
				.name(title)
				.tenantId(tenantId)
				.variables(variables)
				.start();
			skipFirstTaskIfConfigured(processDefinition, processInstance);
			return buildRuntimeVO(processInstance, processDefinition.getName(), serialNumber);
		} finally {
			identityService.setAuthenticatedUserId(null);
		}
	}

	private void ensureBuiltInDefinition(String definitionKey, String tenantId) {
		String resourceName = builtInResource(definitionKey);
		if (!StringUtils.hasText(resourceName)) {
			return;
		}
		long definitionCount = repositoryService.createProcessDefinitionQuery()
			.processDefinitionTenantId(tenantId)
			.processDefinitionKey(definitionKey)
			.count();
		if (definitionCount > 0) {
			return;
		}
		byte[] bpmnBytes = readBuiltInResource(resourceName);
		Deployment deployment = repositoryService.createDeployment()
			.name(builtInName(definitionKey))
			.tenantId(tenantId)
			.addBytes(resourceName, bpmnBytes)
			.deploy();
		createBuiltInModel(definitionKey, tenantId, deployment.getId(), bpmnBytes);
	}

	private void createBuiltInModel(String definitionKey, String tenantId, String deploymentId, byte[] bpmnBytes) {
		long modelCount = repositoryService.createModelQuery()
			.modelTenantId(tenantId)
			.modelKey(definitionKey)
			.count();
		if (modelCount > 0) {
			return;
		}
		Model model = repositoryService.newModel();
		model.setKey(definitionKey);
		model.setName(builtInName(definitionKey));
		model.setCategory("招采服务");
		model.setTenantId(tenantId);
		model.setVersion(1);
		model.setDeploymentId(deploymentId);
		repositoryService.saveModel(model);
		repositoryService.addModelEditorSource(model.getId(), bpmnBytes);
	}

	private byte[] readBuiltInResource(String resourceName) {
		try (InputStream inputStream = new ClassPathResource(resourceName).getInputStream()) {
			return inputStream.readAllBytes();
		} catch (IOException exception) {
			throw new ServiceException("读取内置流程资源失败: " + resourceName);
		}
	}

	private String builtInResource(String definitionKey) {
		return switch (definitionKey) {
			case PROCUREMENT_REQUIREMENT_APPROVAL -> PROCUREMENT_REQUIREMENT_APPROVAL_RESOURCE;
			case PROCUREMENT_PURCHASE_REQUIREMENT_APPROVAL -> PROCUREMENT_PURCHASE_REQUIREMENT_APPROVAL_RESOURCE;
			case PROCUREMENT_BIDDING_REQUIREMENT_APPROVAL -> PROCUREMENT_BIDDING_REQUIREMENT_APPROVAL_RESOURCE;
			case PROCUREMENT_REVIEW_EXPERT_ADMISSION_APPROVAL -> PROCUREMENT_REVIEW_EXPERT_ADMISSION_APPROVAL_RESOURCE;
			case PROCUREMENT_REVIEW_EXPERT_RETIREMENT_APPROVAL -> PROCUREMENT_REVIEW_EXPERT_RETIREMENT_APPROVAL_RESOURCE;
			default -> null;
		};
	}

	private String builtInName(String definitionKey) {
		return switch (definitionKey) {
			case PROCUREMENT_PURCHASE_REQUIREMENT_APPROVAL -> "采购需求审批";
			case PROCUREMENT_BIDDING_REQUIREMENT_APPROVAL -> "竞价需求审批";
			case PROCUREMENT_REVIEW_EXPERT_ADMISSION_APPROVAL -> "评标专家准入审批";
			case PROCUREMENT_REVIEW_EXPERT_RETIREMENT_APPROVAL -> "评标专家清退审批";
			default -> "采购需求审批";
		};
	}

	@Override
	public IPage<ProcessInstanceVO> mine(Integer current, Integer size) {
		PageRequest pageRequest = resolvePage(current, size);
		HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
			.processInstanceTenantId(currentTenantId())
			.startedBy(currentUserId());
		long total = query.count();
		List<ProcessInstanceVO> records = query.orderByProcessInstanceStartTime().desc()
			.listPage(pageRequest.firstResult(), pageRequest.pageSize())
			.stream()
			.map(this::buildHistoricVO)
			.toList();
		Page<ProcessInstanceVO> page = new Page<>(pageRequest.pageNumber(), pageRequest.pageSize(), total);
		page.setRecords(records);
		return page;
	}

	@Override
	public ProcessInstanceVO detail(String processInstanceId) {
		return buildHistoricVO(getTenantHistoricProcess(processInstanceId));
	}

	@Override
	public List<ProcessHistoryVO> history(String processInstanceId) {
		getTenantHistoricProcess(processInstanceId);
		Map<String, String> comments = taskService.getProcessInstanceComments(processInstanceId)
			.stream()
			.filter(comment -> StringUtils.hasText(comment.getTaskId()) && StringUtils.hasText(comment.getFullMessage()))
			.collect(Collectors.groupingBy(Comment::getTaskId,
				Collectors.mapping(Comment::getFullMessage, Collectors.joining("\n"))));
		List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
			.processInstanceId(processInstanceId)
			.orderByHistoricActivityInstanceStartTime().asc()
			.list();
		return activities.stream().map(activity -> buildHistoryVO(activity, comments)).toList();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean cancel(String processInstanceId, String reason) {
		if (!StringUtils.hasText(processInstanceId)) {
			throw new ServiceException("流程实例ID不能为空");
		}
		String tenantId = currentTenantId();
		ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
			.processInstanceId(processInstanceId)
			.processInstanceTenantId(tenantId)
			.singleResult();
		if (processInstance == null) {
			throw new ServiceException("运行中的流程实例不存在或无权访问");
		}
		HistoricProcessInstance historicProcess = getTenantHistoricProcess(processInstanceId);
		boolean owner = currentUserId().equals(historicProcess.getStartUserId());
		if (!owner && !SecureUtil.isAdmin() && !SecureUtil.isAdministrator()) {
			throw new ServiceException("仅流程发起人或管理员可以撤销流程");
		}
		runtimeService.deleteProcessInstance(processInstanceId,
			StringUtils.hasText(reason) ? reason.trim() : DEFAULT_CANCEL_REASON);
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean remove(String processInstanceId) {
		HistoricProcessInstance historicProcess = getTenantHistoricProcess(processInstanceId);
		ProcessInstance runtimeInstance = runtimeService.createProcessInstanceQuery()
			.processInstanceId(processInstanceId)
			.processInstanceTenantId(currentTenantId())
			.singleResult();
		if (runtimeInstance != null) {
			throw new ServiceException("运行中的流程不能删除，请先撤销或终止流程");
		}
		boolean owner = currentUserId().equals(historicProcess.getStartUserId());
		if (!owner && !SecureUtil.isAdmin() && !SecureUtil.isAdministrator()) {
			throw new ServiceException("仅流程发起人或管理员可以删除流程记录");
		}
		historyService.deleteHistoricProcessInstance(processInstanceId);
		return true;
	}

	private HistoricProcessInstance getTenantHistoricProcess(String processInstanceId) {
		if (!StringUtils.hasText(processInstanceId)) {
			throw new ServiceException("流程实例ID不能为空");
		}
		HistoricProcessInstance historicProcess = historyService.createHistoricProcessInstanceQuery()
			.processInstanceId(processInstanceId)
			.processInstanceTenantId(currentTenantId())
			.singleResult();
		if (historicProcess == null) {
			throw new ServiceException("流程实例不存在或无权访问");
		}
		return historicProcess;
	}

	private ProcessInstanceVO buildRuntimeVO(ProcessInstance processInstance, String processDefinitionName,
									 String serialNumber) {
		ProcessInstanceVO vo = new ProcessInstanceVO();
		vo.setId(processInstance.getId());
		vo.setProcessDefinitionId(processInstance.getProcessDefinitionId());
		vo.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
		vo.setProcessDefinitionName(processDefinitionName);
		vo.setTitle(processInstance.getName());
		vo.setSerialNumber(serialNumber);
		vo.setBusinessKey(processInstance.getBusinessKey());
		vo.setStartUserId(processInstance.getStartUserId());
		vo.setStartTime(processInstance.getStartTime());
		vo.setSuspended(processInstance.isSuspended());
		vo.setState(processInstance.isSuspended() ? "SUSPENDED" : "RUNNING");
		vo.setTenantId(processInstance.getTenantId());
		return vo;
	}

	private ProcessInstanceVO buildHistoricVO(HistoricProcessInstance processInstance) {
		ProcessInstance runtimeInstance = null;
		if (processInstance.getEndTime() == null) {
			runtimeInstance = runtimeService.createProcessInstanceQuery()
				.processInstanceId(processInstance.getId())
				.processInstanceTenantId(processInstance.getTenantId())
				.singleResult();
		}
		ProcessInstanceVO vo = new ProcessInstanceVO();
		vo.setId(processInstance.getId());
		vo.setProcessDefinitionId(processInstance.getProcessDefinitionId());
		vo.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
		vo.setProcessDefinitionName(processInstance.getProcessDefinitionName());
		vo.setTitle(processInstance.getName());
		vo.setSerialNumber(getHistoricVariable(processInstance.getId(), SERIAL_VARIABLE));
		vo.setBusinessKey(processInstance.getBusinessKey());
		vo.setStartUserId(processInstance.getStartUserId());
		vo.setStartTime(processInstance.getStartTime());
		vo.setEndTime(processInstance.getEndTime());
		vo.setDeleteReason(processInstance.getDeleteReason());
		vo.setTenantId(processInstance.getTenantId());
		boolean suspended = runtimeInstance != null && runtimeInstance.isSuspended();
		vo.setSuspended(suspended);
		if (runtimeInstance != null) {
			vo.setState(suspended ? "SUSPENDED" : "RUNNING");
		} else if (StringUtils.hasText(processInstance.getDeleteReason())) {
			vo.setState("CANCELED");
		} else {
			vo.setState("COMPLETED");
		}
		return vo;
	}

	private String generateSerialNumber(ProcessDefinition definition, String tenantId) {
		FlowDesignConfigService.SerialConfig config = flowDesignConfigService.serialConfig(definition.getId());
		if (!config.isEnabled()) {
			return null;
		}
		LocalDateTime now = LocalDateTime.now();
		String datePart = formatDate(now, config.getDateFormat(), "yyyyMMdd");
		String cyclePart = switch (String.valueOf(config.getResetCycle())) {
			case "day" -> now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
			case "month" -> now.format(DateTimeFormatter.ofPattern("yyyyMM"));
			case "year" -> now.format(DateTimeFormatter.ofPattern("yyyy"));
			default -> "all";
		};
		String counterKey = "blade:flow:serial:" + tenantId + ":" + definition.getKey() + ":" + cyclePart;
		Long sequence = bladeRedis.incr(counterKey);
		if (sequence != null && sequence == 1L && config.getInitial() > 0) {
			sequence = bladeRedis.incrBy(counterKey, config.getInitial());
		}
		long value = sequence == null ? config.getInitial() + 1 : sequence;
		int digits = Math.max(1, Math.min(config.getDigits(), 12));
		String numericPart = String.format("%0" + digits + "d", value);
		List<String> parts = new ArrayList<>();
		if (StringUtils.hasText(config.getPrefix())) {
			parts.add(config.getPrefix().trim());
		}
		if (StringUtils.hasText(datePart)) {
			parts.add(datePart);
		}
		parts.add(numericPart);
		return String.join(config.getConnector() == null ? "" : config.getConnector(), parts);
	}

	private String resolveProcessTitle(ProcessDefinition definition, Map<String, Object> variables, String userId) {
		String template = flowDesignConfigService.processProperty(definition.getId(), "titleTemplate");
		if (!StringUtils.hasText(template)) {
			return definition.getName();
		}
		Matcher matcher = TITLE_TOKEN_PATTERN.matcher(template);
		StringBuilder result = new StringBuilder();
		while (matcher.find()) {
			String replacement = resolveTitleToken(matcher.group(1), definition, variables, userId);
			matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	private String resolveTitleToken(String token, ProcessDefinition definition,
								 Map<String, Object> variables, String userId) {
		return switch (token) {
			case "processDefId" -> definition.getId();
			case "processDefName" -> definition.getName();
			case "processDefKey" -> definition.getKey();
			case "applyUser" -> userId;
			case "applyUserName" -> String.valueOf(SecureUtil.getUserName());
			default -> {
				Matcher dateMatcher = DATE_FUNCTION_PATTERN.matcher(token);
				if (dateMatcher.matches()) {
					yield formatDate(LocalDateTime.now(), dateMatcher.group(1), "yyyyMMddHHmm");
				}
				Object value = variables.get(token);
				yield value == null ? "" : String.valueOf(value);
			}
		};
	}

	private String formatDate(LocalDateTime dateTime, String pattern, String fallbackPattern) {
		try {
			return dateTime.format(DateTimeFormatter.ofPattern(
				StringUtils.hasText(pattern) ? pattern : fallbackPattern));
		} catch (IllegalArgumentException exception) {
			throw new ServiceException("流程日期格式无效: " + pattern);
		}
	}

	private void skipFirstTaskIfConfigured(ProcessDefinition definition, ProcessInstance processInstance) {
		if (!"true".equals(flowDesignConfigService.processProperty(definition.getId(), "skipFirstNode"))) {
			return;
		}
		List<org.flowable.task.api.Task> tasks = taskService.createTaskQuery()
			.processInstanceId(processInstance.getId())
			.orderByTaskCreateTime().asc()
			.list();
		if (tasks.size() != 1) {
			throw new ServiceException("跳过第一节点仅支持启动后产生一个用户任务的流程");
		}
		taskService.complete(tasks.getFirst().getId());
	}

	private String getHistoricVariable(String processInstanceId, String variableName) {
		HistoricVariableInstance variable = historyService.createHistoricVariableInstanceQuery()
			.processInstanceId(processInstanceId)
			.variableName(variableName)
			.singleResult();
		return variable == null || variable.getValue() == null ? null : String.valueOf(variable.getValue());
	}

	private ProcessHistoryVO buildHistoryVO(HistoricActivityInstance activity, Map<String, String> comments) {
		ProcessHistoryVO vo = new ProcessHistoryVO();
		vo.setActivityId(activity.getActivityId());
		vo.setActivityName(activity.getActivityName());
		vo.setActivityType(activity.getActivityType());
		vo.setTaskId(activity.getTaskId());
		vo.setAssignee(activity.getAssignee());
		vo.setStartTime(activity.getStartTime());
		vo.setEndTime(activity.getEndTime());
		vo.setDuration(activity.getDurationInMillis());
		vo.setComment(comments.get(activity.getTaskId()));
		return vo;
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
