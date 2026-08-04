/**
 * Copyright (c) 2018-2099, Chill Zhuang 庄骞 (bladejava@qq.com).
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
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.secure.utils.SecureUtil;
import org.springblade.flow.service.IProcessDefinitionService;
import org.springblade.flow.vo.ProcessDefinitionVO;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

/**
 * 流程定义服务实现类
 *
 * @author Chill
 */
@Slf4j
@Service
@AllArgsConstructor
public class ProcessDefinitionServiceImpl implements IProcessDefinitionService {

	private static final int DEFAULT_PAGE_NUMBER = 1;
	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final int MAX_PAGE_SIZE = 100;
	private static final long MAX_BPMN_FILE_SIZE = 5L * 1024L * 1024L;

	private final RepositoryService repositoryService;
	private final RuntimeService runtimeService;
	private final HistoryService historyService;

	@Override
	public IPage<ProcessDefinitionVO> page(Integer current, Integer size, String key, String name) {
		int pageNumber = current == null || current < DEFAULT_PAGE_NUMBER ? DEFAULT_PAGE_NUMBER : current;
		int pageSize = size == null || size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
		long firstResult = (long) (pageNumber - 1) * pageSize;
		if (firstResult > Integer.MAX_VALUE) {
			throw new ServiceException("分页参数超出允许范围");
		}

		ProcessDefinitionQuery processDefinitionQuery = tenantProcessDefinitionQuery();
		if (StringUtils.hasText(key)) {
			processDefinitionQuery.processDefinitionKey(key.trim());
		}
		if (StringUtils.hasText(name)) {
			processDefinitionQuery.processDefinitionNameLike("%" + name.trim() + "%");
		}

		long total = processDefinitionQuery.count();
		List<ProcessDefinitionVO> records = processDefinitionQuery
			.orderByProcessDefinitionKey().asc()
			.orderByProcessDefinitionVersion().desc()
			.listPage((int) firstResult, pageSize)
			.stream()
			.map(this::buildProcessDefinitionVO)
			.toList();

		Page<ProcessDefinitionVO> page = new Page<>(pageNumber, pageSize, total);
		page.setRecords(records);
		return page;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessDefinitionVO deploy(MultipartFile file, String category) {
		String resourceName = validateBpmnFile(file);
		String tenantId = currentTenantId();

		try (InputStream inputStream = file.getInputStream()) {
			DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
				.name(resourceName)
				.tenantId(tenantId)
				.addInputStream(resourceName, inputStream);
			if (StringUtils.hasText(category)) {
				deploymentBuilder.category(category.trim());
			}

			Deployment deployment = deploymentBuilder.deploy();
			ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.deploymentId(deployment.getId())
				.processDefinitionTenantId(tenantId)
				.orderByProcessDefinitionVersion().desc()
				.list()
				.stream()
				.findFirst()
				.orElseThrow(() -> new ServiceException("流程部署成功，但未解析到流程定义"));
			return buildProcessDefinitionVO(processDefinition, deployment);
		} catch (IOException exception) {
			log.error("读取BPMN流程文件失败，文件名：{}", resourceName, exception);
			throw new ServiceException("读取BPMN流程文件失败");
		}
	}

	@Override
	public boolean suspend(String processDefinitionId) {
		ProcessDefinition processDefinition = getTenantProcessDefinition(processDefinitionId);
		if (processDefinition.isSuspended()) {
			return true;
		}
		repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);
		return true;
	}

	@Override
	public boolean activate(String processDefinitionId) {
		ProcessDefinition processDefinition = getTenantProcessDefinition(processDefinitionId);
		if (!processDefinition.isSuspended()) {
			return true;
		}
		repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);
		return true;
	}

	@Override
	public Resource download(String processDefinitionId) {
		ProcessDefinition processDefinition = getTenantProcessDefinition(processDefinitionId);
		try (InputStream inputStream = repositoryService.getProcessModel(processDefinitionId)) {
			if (inputStream == null) {
				throw new ServiceException("流程定义不存在BPMN资源");
			}
			byte[] resourceBytes = inputStream.readAllBytes();
			String resourceName = processDefinition.getResourceName();
			return new ByteArrayResource(resourceBytes) {
				@Override
				public String getFilename() {
					return resourceName;
				}
			};
		} catch (IOException exception) {
			log.error("读取流程定义资源失败，流程定义ID：{}", processDefinitionId, exception);
			throw new ServiceException("读取流程定义资源失败");
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean remove(String processDefinitionId) {
		ProcessDefinition processDefinition = getTenantProcessDefinition(processDefinitionId);
		long runtimeCount = runtimeService.createProcessInstanceQuery()
			.processDefinitionId(processDefinitionId)
			.processInstanceTenantId(currentTenantId())
			.count();
		if (runtimeCount > 0) {
			throw new ServiceException("该流程定义仍有运行中的实例，请先撤销或终止流程");
		}
		long historyCount = historyService.createHistoricProcessInstanceQuery()
			.processDefinitionId(processDefinitionId)
			.processInstanceTenantId(currentTenantId())
			.count();
		if (historyCount > 0) {
			throw new ServiceException("该流程定义仍有关联历史实例，请先删除对应流程记录");
		}
		String deploymentId = processDefinition.getDeploymentId();
		List<Model> models = repositoryService.createModelQuery()
			.modelTenantId(currentTenantId())
			.list()
			.stream()
			.filter(model -> deploymentId.equals(model.getDeploymentId()))
			.toList();
		repositoryService.deleteDeployment(deploymentId, false);
		models.forEach(model -> {
			model.setDeploymentId(null);
			repositoryService.saveModel(model);
		});
		return true;
	}

	private ProcessDefinitionQuery tenantProcessDefinitionQuery() {
		return repositoryService.createProcessDefinitionQuery()
			.processDefinitionTenantId(currentTenantId());
	}

	private ProcessDefinition getTenantProcessDefinition(String processDefinitionId) {
		if (!StringUtils.hasText(processDefinitionId)) {
			throw new ServiceException("流程定义ID不能为空");
		}
		ProcessDefinition processDefinition = tenantProcessDefinitionQuery()
			.processDefinitionId(processDefinitionId)
			.singleResult();
		if (processDefinition == null) {
			throw new ServiceException("流程定义不存在或无权访问");
		}
		return processDefinition;
	}

	private ProcessDefinitionVO buildProcessDefinitionVO(ProcessDefinition processDefinition) {
		Deployment deployment = repositoryService.createDeploymentQuery()
			.deploymentId(processDefinition.getDeploymentId())
			.singleResult();
		return buildProcessDefinitionVO(processDefinition, deployment);
	}

	private ProcessDefinitionVO buildProcessDefinitionVO(ProcessDefinition processDefinition, Deployment deployment) {
		ProcessDefinitionVO processDefinitionVO = new ProcessDefinitionVO();
		processDefinitionVO.setId(processDefinition.getId());
		processDefinitionVO.setKey(processDefinition.getKey());
		processDefinitionVO.setName(processDefinition.getName());
		processDefinitionVO.setCategory(processDefinition.getCategory());
		processDefinitionVO.setVersion(processDefinition.getVersion());
		processDefinitionVO.setDeploymentId(processDefinition.getDeploymentId());
		processDefinitionVO.setResourceName(processDefinition.getResourceName());
		processDefinitionVO.setTenantId(processDefinition.getTenantId());
		processDefinitionVO.setSuspended(processDefinition.isSuspended());
		if (deployment != null) {
			processDefinitionVO.setDeploymentTime(deployment.getDeploymentTime());
		}
		return processDefinitionVO;
	}

	private String validateBpmnFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ServiceException("BPMN流程文件不能为空");
		}
		if (file.getSize() > MAX_BPMN_FILE_SIZE) {
			throw new ServiceException("BPMN流程文件不能超过5MB");
		}

		String originalFilename = file.getOriginalFilename();
		if (!StringUtils.hasText(originalFilename)) {
			throw new ServiceException("BPMN流程文件名不能为空");
		}
		String resourceName = StringUtils.cleanPath(originalFilename.trim());
		if (resourceName.contains("..")) {
			throw new ServiceException("BPMN流程文件名不合法");
		}
		String lowerCaseFilename = resourceName.toLowerCase(Locale.ROOT);
		if (!lowerCaseFilename.endsWith(".bpmn") && !lowerCaseFilename.endsWith(".bpmn20.xml")) {
			throw new ServiceException("仅支持.bpmn或.bpmn20.xml格式的流程文件");
		}
		return resourceName;
	}

	private String currentTenantId() {
		String tenantId = SecureUtil.getTenantId();
		if (!StringUtils.hasText(tenantId)) {
			throw new ServiceException("无法获取当前租户信息");
		}
		return tenantId;
	}

}
