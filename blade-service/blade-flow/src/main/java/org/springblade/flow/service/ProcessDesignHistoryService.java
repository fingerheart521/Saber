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
package org.springblade.flow.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Model;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.flow.vo.ProcessDesignHistoryVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程设计历史快照服务
 *
 * @author Chill
 */
@Slf4j
@Service
@AllArgsConstructor
public class ProcessDesignHistoryService {

	public static final String TYPE_MODEL = "model";
	public static final String TYPE_FORM = "form";
	public static final String MODEL_HISTORY_CATEGORY = "__SPRING_BLADE_FLOW_MODEL_HISTORY__";
	public static final String FORM_HISTORY_CATEGORY = "__SPRING_BLADE_FLOW_FORM_HISTORY__";

	private static final String HISTORY_KEY_PREFIX = "HISTORY::";

	private final RepositoryService repositoryService;

	/**
	 * 查询历史版本，并在首次使用时记录当前设计基线。
	 */
	public List<ProcessDesignHistoryVO> list(Model owner, String designType, String designMeta) {
		ensureBaseline(owner, designType, designMeta);
		return historyModels(owner, designType).stream()
			.sorted(Comparator.comparing(Model::getVersion, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(Model::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
			.map(historyModel -> buildVO(owner, historyModel, false))
			.toList();
	}

	/**
	 * 查询历史版本预览。
	 */
	public ProcessDesignHistoryVO preview(Model owner, String designType, String historyId, String designMeta) {
		ensureBaseline(owner, designType, designMeta);
		return buildVO(owner, getHistoryModel(owner, designType, historyId), true);
	}

	/**
	 * 获取历史快照内容。
	 */
	public DesignSnapshot snapshot(Model owner, String designType, String historyId, String designMeta) {
		ensureBaseline(owner, designType, designMeta);
		Model historyModel = getHistoryModel(owner, designType, historyId);
		HistoryMetaInfo historyMetaInfo = parseMetaInfo(historyModel.getMetaInfo());
		byte[] editorSource = repositoryService.getModelEditorSource(historyModel.getId());
		if (editorSource == null || editorSource.length == 0) {
			throw new ServiceException("历史版本设计内容不存在");
		}
		return new DesignSnapshot(
			new String(editorSource, StandardCharsets.UTF_8),
			historyMetaInfo.getDesignMeta(),
			historyModel.getVersion(),
			historyMetaInfo.getSourceVersion()
		);
	}

	/**
	 * 记录当前主版本设计快照。
	 */
	public void recordCurrent(Model owner, String designType, String designMeta, Integer sourceVersion) {
		byte[] editorSource = repositoryService.getModelEditorSource(owner.getId());
		if (editorSource == null || editorSource.length == 0 || hasVersion(owner, designType, currentVersion(owner))) {
			return;
		}

		Model historyModel = repositoryService.newModel();
		historyModel.setTenantId(owner.getTenantId());
		historyModel.setKey(buildHistoryKey(owner, designType));
		historyModel.setName(owner.getName() + " v" + currentVersion(owner));
		historyModel.setCategory(historyCategory(designType));
		historyModel.setVersion(currentVersion(owner));

		HistoryMetaInfo historyMetaInfo = new HistoryMetaInfo();
		historyMetaInfo.setOwnerId(owner.getId());
		historyMetaInfo.setDesignType(designType);
		historyMetaInfo.setDesignMeta(designMeta);
		historyMetaInfo.setSourceVersion(sourceVersion);
		historyModel.setMetaInfo(JsonUtil.toJson(historyMetaInfo));
		repositoryService.saveModel(historyModel);
		repositoryService.addModelEditorSource(historyModel.getId(), editorSource);
	}

	/**
	 * 删除业务数据对应的全部历史快照。
	 */
	public void removeAll(Model owner, String designType) {
		historyModels(owner, designType).forEach(historyModel -> repositoryService.deleteModel(historyModel.getId()));
	}

	/**
	 * 判断是否为设计历史保留分类。
	 */
	public boolean isHistoryCategory(String category) {
		return MODEL_HISTORY_CATEGORY.equals(category) || FORM_HISTORY_CATEGORY.equals(category);
	}

	private void ensureBaseline(Model owner, String designType, String designMeta) {
		recordCurrent(owner, designType, designMeta, null);
	}

	private List<Model> historyModels(Model owner, String designType) {
		return repositoryService.createModelQuery()
			.modelTenantId(owner.getTenantId())
			.modelCategory(historyCategory(designType))
			.list()
			.stream()
			.filter(historyModel -> {
				HistoryMetaInfo historyMetaInfo = parseMetaInfo(historyModel.getMetaInfo());
				return Objects.equals(owner.getId(), historyMetaInfo.getOwnerId())
					&& Objects.equals(designType, historyMetaInfo.getDesignType());
			})
			.toList();
	}

	private Model getHistoryModel(Model owner, String designType, String historyId) {
		if (!StringUtils.hasText(historyId)) {
			throw new ServiceException("历史快照ID不能为空");
		}
		Model historyModel = repositoryService.createModelQuery()
			.modelTenantId(owner.getTenantId())
			.modelCategory(historyCategory(designType))
			.modelId(historyId.trim())
			.singleResult();
		if (historyModel == null) {
			throw new ServiceException("设计历史版本不存在或无权访问");
		}
		HistoryMetaInfo historyMetaInfo = parseMetaInfo(historyModel.getMetaInfo());
		if (!Objects.equals(owner.getId(), historyMetaInfo.getOwnerId())
			|| !Objects.equals(designType, historyMetaInfo.getDesignType())) {
			throw new ServiceException("设计历史版本不存在或无权访问");
		}
		return historyModel;
	}

	private ProcessDesignHistoryVO buildVO(Model owner, Model historyModel, boolean includeContent) {
		HistoryMetaInfo historyMetaInfo = parseMetaInfo(historyModel.getMetaInfo());
		ProcessDesignHistoryVO historyVO = new ProcessDesignHistoryVO();
		historyVO.setId(historyModel.getId());
		historyVO.setBusinessId(owner.getId());
		historyVO.setDesignType(historyMetaInfo.getDesignType());
		historyVO.setBusinessKey(owner.getKey());
		historyVO.setBusinessName(owner.getName());
		historyVO.setVersion(historyModel.getVersion());
		Integer sourceVersion = historyMetaInfo.getSourceVersion();
		historyVO.setSourceVersion(sourceVersion != null && sourceVersion > 0 ? sourceVersion : null);
		historyVO.setCurrent(Objects.equals(currentVersion(owner), historyModel.getVersion()));
		historyVO.setCreateTime(historyModel.getCreateTime());
		if (includeContent) {
			byte[] editorSource = repositoryService.getModelEditorSource(historyModel.getId());
			historyVO.setDesignContent(editorSource == null || editorSource.length == 0
				? null
				: new String(editorSource, StandardCharsets.UTF_8));
		}
		return historyVO;
	}

	private boolean hasVersion(Model owner, String designType, Integer version) {
		return historyModels(owner, designType).stream()
			.anyMatch(historyModel -> Objects.equals(version, historyModel.getVersion()));
	}

	private String buildHistoryKey(Model owner, String designType) {
		return HISTORY_KEY_PREFIX + designType.toUpperCase() + "::" + owner.getId() + "::"
			+ currentVersion(owner) + "::" + UUID.randomUUID().toString().replace("-", "");
	}

	private String historyCategory(String designType) {
		return switch (designType) {
			case TYPE_MODEL -> MODEL_HISTORY_CATEGORY;
			case TYPE_FORM -> FORM_HISTORY_CATEGORY;
			default -> throw new ServiceException("不支持的设计历史类型");
		};
	}

	private Integer currentVersion(Model owner) {
		return owner.getVersion() == null ? 1 : owner.getVersion();
	}

	private HistoryMetaInfo parseMetaInfo(String metaInfo) {
		if (!StringUtils.hasText(metaInfo)) {
			return new HistoryMetaInfo();
		}
		try {
			HistoryMetaInfo historyMetaInfo = JsonUtil.parse(metaInfo, HistoryMetaInfo.class);
			return historyMetaInfo == null ? new HistoryMetaInfo() : historyMetaInfo;
		} catch (Exception exception) {
			log.warn("解析流程设计历史元数据失败，内容：{}", metaInfo, exception);
			return new HistoryMetaInfo();
		}
	}

	/**
	 * 设计历史快照内容。
	 */
	public record DesignSnapshot(String designContent, String designMeta, Integer version, Integer sourceVersion) {
	}

	@Data
	private static final class HistoryMetaInfo {

		private String ownerId;
		private String designType;
		private String designMeta;
		private Integer sourceVersion;

	}

}
