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
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.springblade.core.secure.utils.SecureUtil;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.flow.dto.ProcessDesignCopyDTO;
import org.springblade.flow.dto.ProcessFormDTO;
import org.springblade.flow.dto.ProcessFormDesignDTO;
import org.springblade.flow.service.IProcessFormService;
import org.springblade.flow.service.ProcessDesignHistoryService;
import org.springblade.flow.vo.ProcessDesignHistoryVO;
import org.springblade.flow.vo.ProcessFormVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 流程表单服务实现
 *
 * @author Chill
 */
@Slf4j
@Service
@AllArgsConstructor
public class ProcessFormServiceImpl implements IProcessFormService {

	private static final int DEFAULT_PAGE_NUMBER = 1;
	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final int MAX_PAGE_SIZE = 100;
	private static final int ENABLED_STATUS = 1;
	private static final String FORM_MODEL_CATEGORY = "__SPRING_BLADE_FLOW_FORM__";
	private static final String FORM_MODEL_KEY_PREFIX = "FORM::";
	private static final String EMPTY_FORM_JSON = "{\"labelPosition\":\"right\",\"labelWidth\":100,\"size\":\"default\",\"columns\":[]}";
	private static final Pattern FORM_KEY_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_.-]*$");

	private final RepositoryService repositoryService;
	private final ProcessDesignHistoryService processDesignHistoryService;

	@Override
	public IPage<ProcessFormVO> page(Integer current, Integer size, String key, String name, String category, Integer status) {
		int pageNumber = current == null || current < DEFAULT_PAGE_NUMBER ? DEFAULT_PAGE_NUMBER : current;
		int pageSize = size == null || size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
		List<ProcessFormVO> filteredForms = tenantFormModels().stream()
			.map(model -> buildFormVO(model, false))
			.filter(form -> !StringUtils.hasText(key) || containsIgnoreCase(form.getKey(), key))
			.filter(form -> !StringUtils.hasText(name) || containsIgnoreCase(form.getName(), name))
			.filter(form -> !StringUtils.hasText(category) || Objects.equals(form.getCategory(), category.trim()))
			.filter(form -> status == null || Objects.equals(form.getStatus(), status))
			.sorted(Comparator.comparing(ProcessFormVO::getLastUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
			.toList();

		long firstResult = (long) (pageNumber - 1) * pageSize;
		int fromIndex = firstResult >= filteredForms.size() ? filteredForms.size() : (int) firstResult;
		int toIndex = Math.min(fromIndex + pageSize, filteredForms.size());
		Page<ProcessFormVO> page = new Page<>(pageNumber, pageSize, filteredForms.size());
		page.setRecords(filteredForms.subList(fromIndex, toIndex));
		return page;
	}

	@Override
	public List<String> categories() {
		return tenantFormModels().stream()
			.map(model -> parseMetaInfo(model.getMetaInfo()).getCategory())
			.filter(StringUtils::hasText)
			.distinct()
			.sorted()
			.toList();
	}

	@Override
	public List<ProcessFormVO> options() {
		return tenantFormModels().stream()
			.map(model -> buildFormVO(model, false))
			.filter(form -> Objects.equals(form.getStatus(), ENABLED_STATUS))
			.sorted(Comparator.comparing(ProcessFormVO::getName, String.CASE_INSENSITIVE_ORDER))
			.toList();
	}

	@Override
	public ProcessFormVO detail(String formId) {
		return buildFormVO(getTenantForm(formId), true);
	}

	@Override
	public ProcessFormVO detailByKey(String formKey) {
		if (!StringUtils.hasText(formKey)) {
			throw new ServiceException("表单key不能为空");
		}
		Model model = repositoryService.createModelQuery()
			.modelTenantId(currentTenantId())
			.modelCategory(FORM_MODEL_CATEGORY)
			.modelKey(toStorageKey(formKey.trim()))
			.singleResult();
		if (model == null || !Objects.equals(buildFormVO(model, false).getStatus(), ENABLED_STATUS)) {
			throw new ServiceException("流程表单不存在、未启用或无权访问");
		}
		return buildFormVO(model, true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessFormVO save(ProcessFormDTO processFormDTO) {
		String tenantId = currentTenantId();
		String formKey = processFormDTO.getKey().trim();
		validateFormKey(formKey, processFormDTO.getId(), tenantId);
		Model model = StringUtils.hasText(processFormDTO.getId())
			? getTenantForm(processFormDTO.getId())
			: repositoryService.newModel();

		if (!StringUtils.hasText(model.getId())) {
			model.setTenantId(tenantId);
			model.setVersion(1);
		}
		model.setKey(toStorageKey(formKey));
		model.setName(processFormDTO.getName().trim());
		model.setCategory(FORM_MODEL_CATEGORY);

		FormMetaInfo metaInfo = parseMetaInfo(model.getMetaInfo());
		metaInfo.setCategory(processFormDTO.getCategory().trim());
		metaInfo.setStatus(processFormDTO.getStatus() == null ? ENABLED_STATUS : processFormDTO.getStatus());
		metaInfo.setRemark(trimToNull(processFormDTO.getRemark()));
		model.setMetaInfo(JsonUtil.toJson(metaInfo));
		repositoryService.saveModel(model);
		if (!model.hasEditorSource()) {
			repositoryService.addModelEditorSource(model.getId(), EMPTY_FORM_JSON.getBytes(StandardCharsets.UTF_8));
		}
		return buildFormVO(model, false);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessFormVO saveDesign(ProcessFormDesignDTO processFormDesignDTO) {
		Model model = getTenantForm(processFormDesignDTO.getFormId());
		processDesignHistoryService.recordCurrent(
			model,
			ProcessDesignHistoryService.TYPE_FORM,
			null,
			null
		);
		String formJson = processFormDesignDTO.getFormJson().trim();
		validateFormJson(formJson);
		model.setVersion(model.getVersion() == null ? 1 : model.getVersion() + 1);
		repositoryService.saveModel(model);
		repositoryService.addModelEditorSource(model.getId(), formJson.getBytes(StandardCharsets.UTF_8));
		processDesignHistoryService.recordCurrent(
			model,
			ProcessDesignHistoryService.TYPE_FORM,
			null,
			null
		);
		return buildFormVO(model, true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessFormVO copy(ProcessDesignCopyDTO processDesignCopyDTO) {
		Model sourceModel = getTenantForm(processDesignCopyDTO.getSourceId());
		String tenantId = currentTenantId();
		String formKey = processDesignCopyDTO.getKey().trim();
		validateFormKey(formKey, null, tenantId);

		Model copiedModel = repositoryService.newModel();
		copiedModel.setTenantId(tenantId);
		copiedModel.setVersion(1);
		copiedModel.setKey(toStorageKey(formKey));
		copiedModel.setName(processDesignCopyDTO.getName().trim());
		copiedModel.setCategory(FORM_MODEL_CATEGORY);
		copiedModel.setMetaInfo(JsonUtil.toJson(parseMetaInfo(sourceModel.getMetaInfo())));
		repositoryService.saveModel(copiedModel);

		byte[] editorSource = repositoryService.getModelEditorSource(sourceModel.getId());
		String formJson = editorSource == null || editorSource.length == 0
			? EMPTY_FORM_JSON
			: new String(editorSource, StandardCharsets.UTF_8);
		validateFormJson(formJson);
		repositoryService.addModelEditorSource(copiedModel.getId(), formJson.getBytes(StandardCharsets.UTF_8));
		processDesignHistoryService.recordCurrent(
			copiedModel,
			ProcessDesignHistoryService.TYPE_FORM,
			null,
			null
		);
		return buildFormVO(copiedModel, true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public List<ProcessDesignHistoryVO> history(String formId) {
		List<ProcessDesignHistoryVO> historyList = processDesignHistoryService.list(
			getTenantForm(formId),
			ProcessDesignHistoryService.TYPE_FORM,
			null
		);
		historyList.forEach(this::normalizeHistoryKey);
		return historyList;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessDesignHistoryVO historyPreview(String formId, String historyId) {
		ProcessDesignHistoryVO historyVO = processDesignHistoryService.preview(
			getTenantForm(formId),
			ProcessDesignHistoryService.TYPE_FORM,
			historyId,
			null
		);
		normalizeHistoryKey(historyVO);
		return historyVO;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessFormVO setMainVersion(String formId, String historyId) {
		Model model = getTenantForm(formId);
		processDesignHistoryService.recordCurrent(
			model,
			ProcessDesignHistoryService.TYPE_FORM,
			null,
			null
		);
		ProcessDesignHistoryService.DesignSnapshot snapshot = processDesignHistoryService.snapshot(
			model,
			ProcessDesignHistoryService.TYPE_FORM,
			historyId,
			null
		);
		validateFormJson(snapshot.designContent());
		model.setVersion(model.getVersion() == null ? 1 : model.getVersion() + 1);
		repositoryService.saveModel(model);
		repositoryService.addModelEditorSource(
			model.getId(),
			snapshot.designContent().getBytes(StandardCharsets.UTF_8)
		);
		processDesignHistoryService.recordCurrent(
			model,
			ProcessDesignHistoryService.TYPE_FORM,
			null,
			snapshot.version()
		);
		return buildFormVO(model, true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean remove(String formId) {
		Model model = getTenantForm(formId);
		processDesignHistoryService.removeAll(model, ProcessDesignHistoryService.TYPE_FORM);
		repositoryService.deleteModel(model.getId());
		return true;
	}

	private List<Model> tenantFormModels() {
		return repositoryService.createModelQuery()
			.modelTenantId(currentTenantId())
			.modelCategory(FORM_MODEL_CATEGORY)
			.list();
	}

	private Model getTenantForm(String formId) {
		if (!StringUtils.hasText(formId)) {
			throw new ServiceException("表单ID不能为空");
		}
		Model model = repositoryService.createModelQuery()
			.modelTenantId(currentTenantId())
			.modelCategory(FORM_MODEL_CATEGORY)
			.modelId(formId.trim())
			.singleResult();
		if (model == null) {
			throw new ServiceException("流程表单不存在或无权访问");
		}
		return model;
	}

	private void validateFormKey(String formKey, String formId, String tenantId) {
		if (!FORM_KEY_PATTERN.matcher(formKey).matches()) {
			throw new ServiceException("表单key必须以字母或下划线开头，且只能包含字母、数字、点、横线和下划线");
		}
		boolean duplicated = repositoryService.createModelQuery()
			.modelTenantId(tenantId)
			.modelKey(toStorageKey(formKey))
			.list()
			.stream()
			.anyMatch(model -> !model.getId().equals(formId));
		if (duplicated) {
			throw new ServiceException("当前租户已存在相同表单key");
		}
	}

	private void validateFormJson(String formJson) {
		try {
			JsonUtil.readTree(formJson);
		} catch (Exception exception) {
			throw new ServiceException("表单设计JSON格式不正确");
		}
	}

	private void normalizeHistoryKey(ProcessDesignHistoryVO historyVO) {
		historyVO.setBusinessKey(fromStorageKey(historyVO.getBusinessKey()));
	}

	private ProcessFormVO buildFormVO(Model model, boolean includeEditorSource) {
		FormMetaInfo metaInfo = parseMetaInfo(model.getMetaInfo());
		ProcessFormVO processFormVO = new ProcessFormVO();
		processFormVO.setId(model.getId());
		processFormVO.setKey(fromStorageKey(model.getKey()));
		processFormVO.setName(model.getName());
		processFormVO.setCategory(metaInfo.getCategory());
		processFormVO.setVersion(model.getVersion());
		processFormVO.setStatus(metaInfo.getStatus() == null ? ENABLED_STATUS : metaInfo.getStatus());
		processFormVO.setRemark(metaInfo.getRemark());
		processFormVO.setTenantId(model.getTenantId());
		processFormVO.setCreateTime(model.getCreateTime());
		processFormVO.setLastUpdateTime(model.getLastUpdateTime());
		processFormVO.setDesigned(model.hasEditorSource());
		if (includeEditorSource) {
			byte[] editorSource = repositoryService.getModelEditorSource(model.getId());
			processFormVO.setFormJson(editorSource == null || editorSource.length == 0
				? EMPTY_FORM_JSON
				: new String(editorSource, StandardCharsets.UTF_8));
		}
		return processFormVO;
	}

	private FormMetaInfo parseMetaInfo(String metaInfo) {
		if (!StringUtils.hasText(metaInfo)) {
			return new FormMetaInfo();
		}
		try {
			FormMetaInfo formMetaInfo = JsonUtil.parse(metaInfo, FormMetaInfo.class);
			return formMetaInfo == null ? new FormMetaInfo() : formMetaInfo;
		} catch (Exception exception) {
			log.warn("解析流程表单元数据失败，将使用空元数据，内容：{}", metaInfo, exception);
			return new FormMetaInfo();
		}
	}

	private boolean containsIgnoreCase(String source, String keyword) {
		return source != null && source.toLowerCase().contains(keyword.trim().toLowerCase());
	}

	private String toStorageKey(String formKey) {
		return FORM_MODEL_KEY_PREFIX + formKey;
	}

	private String fromStorageKey(String storageKey) {
		return storageKey != null && storageKey.startsWith(FORM_MODEL_KEY_PREFIX)
			? storageKey.substring(FORM_MODEL_KEY_PREFIX.length())
			: storageKey;
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private String currentTenantId() {
		String tenantId = SecureUtil.getTenantId();
		if (!StringUtils.hasText(tenantId)) {
			throw new ServiceException("无法获取当前租户信息");
		}
		return tenantId;
	}

	@Data
	private static final class FormMetaInfo {

		private String category;
		private Integer status = ENABLED_STATUS;
		private String remark;

	}

}
