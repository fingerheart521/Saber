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
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.Model;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.secure.utils.SecureUtil;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springblade.flow.dto.ProcessDesignCopyDTO;
import org.springblade.flow.dto.ProcessFormFieldDTO;
import org.springblade.flow.dto.ProcessModelDTO;
import org.springblade.flow.dto.ProcessModelDesignDTO;
import org.springblade.flow.service.IProcessModelService;
import org.springblade.flow.service.ProcessDesignHistoryService;
import org.springblade.flow.vo.ProcessDesignHistoryVO;
import org.springblade.flow.vo.ProcessModelVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 流程模型服务实现类
 *
 * @author Chill
 */
@Slf4j
@Service
@AllArgsConstructor
public class ProcessModelServiceImpl implements IProcessModelService {

	private static final String FORM_MODEL_CATEGORY = "__SPRING_BLADE_FLOW_FORM__";
	private static final int DEFAULT_PAGE_NUMBER = 1;
	private static final int DEFAULT_PAGE_SIZE = 10;
	private static final int MAX_PAGE_SIZE = 100;
	private static final Set<String> FORM_TYPES = Set.of("internal", "external", "independent");
	private static final Pattern MODEL_KEY_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_.-]*$");
	private static final String BPMN_NAMESPACE = "http://www.omg.org/spec/BPMN/20100524/MODEL";
	private static final String BLADE_NAMESPACE = "https://springblade.org/schema/bpmn";
	private static final String DEFAULT_TARGET_NAMESPACE = "http://www.springblade.org/flow";

	private final RepositoryService repositoryService;
	private final ProcessDesignHistoryService processDesignHistoryService;

	@Override
	public IPage<ProcessModelVO> page(Integer current, Integer size, String key, String name, String category) {
		int pageNumber = current == null || current < DEFAULT_PAGE_NUMBER ? DEFAULT_PAGE_NUMBER : current;
		int pageSize = size == null || size < 1 ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
		long firstResult = (long) (pageNumber - 1) * pageSize;
		if (firstResult > Integer.MAX_VALUE) {
			throw new ServiceException("分页参数超出允许范围");
		}

		List<ProcessModelVO> filteredModels = tenantModels().stream()
			.filter(model -> !StringUtils.hasText(key) || Objects.equals(model.getKey(), key.trim()))
			.filter(model -> !StringUtils.hasText(name) || containsIgnoreCase(model.getName(), name))
			.filter(model -> !StringUtils.hasText(category) || Objects.equals(model.getCategory(), category.trim()))
			.sorted(Comparator.comparing(Model::getLastUpdateTime, Comparator.nullsLast(Comparator.reverseOrder())))
			.map(model -> buildModelVO(model, false))
			.toList();
		int fromIndex = firstResult >= filteredModels.size() ? filteredModels.size() : (int) firstResult;
		int toIndex = Math.min(fromIndex + pageSize, filteredModels.size());

		Page<ProcessModelVO> page = new Page<>(pageNumber, pageSize, filteredModels.size());
		page.setRecords(filteredModels.subList(fromIndex, toIndex));
		return page;
	}

	@Override
	public List<String> categories() {
		return tenantModels()
			.stream()
			.map(Model::getCategory)
			.filter(StringUtils::hasText)
			.distinct()
			.sorted()
			.toList();
	}

	@Override
	public ProcessModelVO detail(String modelId) {
		return buildModelVO(getTenantModel(modelId), true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessModelVO save(ProcessModelDTO processModelDTO) {
		String tenantId = currentTenantId();
		String modelKey = processModelDTO.getKey().trim();
		Model model = StringUtils.hasText(processModelDTO.getId())
			? getTenantModel(processModelDTO.getId())
			: repositoryService.newModel();
		validateModelKey(modelKey, model.getId(), tenantId);

		if (!StringUtils.hasText(model.getId())) {
			model.setTenantId(tenantId);
			model.setVersion(1);
		}
		model.setKey(modelKey);
		model.setName(processModelDTO.getName().trim());
		model.setCategory(trimToNull(processModelDTO.getCategory()));

		ModelMetaInfo modelMetaInfo = parseMetaInfo(model.getMetaInfo());
		modelMetaInfo.setDescription(trimToNull(processModelDTO.getDescription()));
		model.setMetaInfo(JsonUtil.toJson(modelMetaInfo));
		repositoryService.saveModel(model);
		return buildModelVO(model, false);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessModelVO saveDesign(ProcessModelDesignDTO processModelDesignDTO) {
		Model model = getTenantModel(processModelDesignDTO.getModelId());
		ModelMetaInfo currentMetaInfo = parseMetaInfo(model.getMetaInfo());
		processDesignHistoryService.recordCurrent(
			model,
			ProcessDesignHistoryService.TYPE_MODEL,
			serializeDesignMeta(currentMetaInfo),
			null
		);
		String formType = processModelDesignDTO.getFormType().trim();
		validateForm(formType, processModelDesignDTO.getFormKey());

		ModelMetaInfo modelMetaInfo = currentMetaInfo;
		modelMetaInfo.setFormType(formType);
		modelMetaInfo.setFormKey(trimToNull(processModelDesignDTO.getFormKey()));
		modelMetaInfo.setFormFields(normalizeFormFields(processModelDesignDTO.getFormFields()));
		model.setMetaInfo(JsonUtil.toJson(modelMetaInfo));
		model.setVersion(model.getVersion() == null ? 1 : model.getVersion() + 1);
		repositoryService.saveModel(model);
		String bpmnXml = synchronizeProcessMetadata(resolveBpmnXml(processModelDesignDTO), model);
		repositoryService.addModelEditorSource(
			model.getId(),
			bpmnXml.getBytes(StandardCharsets.UTF_8)
		);
		processDesignHistoryService.recordCurrent(
			model,
			ProcessDesignHistoryService.TYPE_MODEL,
			serializeDesignMeta(modelMetaInfo),
			null
		);
		return buildModelVO(model, true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessModelVO copy(ProcessDesignCopyDTO processDesignCopyDTO) {
		Model sourceModel = getTenantModel(processDesignCopyDTO.getSourceId());
		String tenantId = currentTenantId();
		String modelKey = processDesignCopyDTO.getKey().trim();
		validateModelKey(modelKey, null, tenantId);

		Model copiedModel = repositoryService.newModel();
		copiedModel.setTenantId(tenantId);
		copiedModel.setVersion(1);
		copiedModel.setKey(modelKey);
		copiedModel.setName(processDesignCopyDTO.getName().trim());
		copiedModel.setCategory(sourceModel.getCategory());
		ModelMetaInfo copiedMetaInfo = parseMetaInfo(sourceModel.getMetaInfo());
		copiedModel.setMetaInfo(JsonUtil.toJson(copiedMetaInfo));
		repositoryService.saveModel(copiedModel);

		byte[] editorSource = repositoryService.getModelEditorSource(sourceModel.getId());
		if (editorSource != null && editorSource.length > 0) {
			String bpmnXml = synchronizeProcessMetadata(
				normalizeBpmnXml(new String(editorSource, StandardCharsets.UTF_8)),
				copiedModel
			);
			repositoryService.addModelEditorSource(copiedModel.getId(), bpmnXml.getBytes(StandardCharsets.UTF_8));
			processDesignHistoryService.recordCurrent(
				copiedModel,
				ProcessDesignHistoryService.TYPE_MODEL,
				serializeDesignMeta(copiedMetaInfo),
				null
			);
		}
		return buildModelVO(copiedModel, true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public List<ProcessDesignHistoryVO> history(String modelId) {
		Model model = getTenantModel(modelId);
		return processDesignHistoryService.list(
			model,
			ProcessDesignHistoryService.TYPE_MODEL,
			serializeDesignMeta(parseMetaInfo(model.getMetaInfo()))
		);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessDesignHistoryVO historyPreview(String modelId, String historyId) {
		Model model = getTenantModel(modelId);
		return processDesignHistoryService.preview(
			model,
			ProcessDesignHistoryService.TYPE_MODEL,
			historyId,
			serializeDesignMeta(parseMetaInfo(model.getMetaInfo()))
		);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessModelVO setMainVersion(String modelId, String historyId) {
		Model model = getTenantModel(modelId);
		ModelMetaInfo modelMetaInfo = parseMetaInfo(model.getMetaInfo());
		String currentDesignMeta = serializeDesignMeta(modelMetaInfo);
		processDesignHistoryService.recordCurrent(
			model,
			ProcessDesignHistoryService.TYPE_MODEL,
			currentDesignMeta,
			null
		);
		ProcessDesignHistoryService.DesignSnapshot snapshot = processDesignHistoryService.snapshot(
			model,
			ProcessDesignHistoryService.TYPE_MODEL,
			historyId,
			currentDesignMeta
		);
		applyDesignMeta(modelMetaInfo, snapshot.designMeta());
		model.setMetaInfo(JsonUtil.toJson(modelMetaInfo));
		model.setVersion(model.getVersion() == null ? 1 : model.getVersion() + 1);
		repositoryService.saveModel(model);
		String bpmnXml = synchronizeProcessMetadata(normalizeBpmnXml(snapshot.designContent()), model);
		repositoryService.addModelEditorSource(model.getId(), bpmnXml.getBytes(StandardCharsets.UTF_8));
		processDesignHistoryService.recordCurrent(
			model,
			ProcessDesignHistoryService.TYPE_MODEL,
			serializeDesignMeta(modelMetaInfo),
			snapshot.version()
		);
		return buildModelVO(model, true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ProcessModelVO deploy(String modelId) {
		Model model = getTenantModel(modelId);
		byte[] editorSource = repositoryService.getModelEditorSource(model.getId());
		if (editorSource == null || editorSource.length == 0) {
			throw new ServiceException("请先完成流程设计再部署");
		}
		String bpmnXml = synchronizeProcessMetadata(
			normalizeBpmnXml(new String(editorSource, StandardCharsets.UTF_8)),
			model
		);
		repositoryService.addModelEditorSource(model.getId(), bpmnXml.getBytes(StandardCharsets.UTF_8));

		String resourceName = model.getKey() + ".bpmn20.xml";
		DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
			.name(model.getName())
			.key(model.getKey())
			.tenantId(currentTenantId())
			.addBytes(resourceName, bpmnXml.getBytes(StandardCharsets.UTF_8));
		if (StringUtils.hasText(model.getCategory())) {
			deploymentBuilder.category(model.getCategory());
		}
		Deployment deployment = deploymentBuilder.deploy();
		model.setDeploymentId(deployment.getId());
		repositoryService.saveModel(model);
		return buildModelVO(model, false);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean remove(String modelId) {
		Model model = getTenantModel(modelId);
		processDesignHistoryService.removeAll(model, ProcessDesignHistoryService.TYPE_MODEL);
		repositoryService.deleteModel(model.getId());
		return true;
	}

	private List<Model> tenantModels() {
		return repositoryService.createModelQuery()
			.modelTenantId(currentTenantId())
			.list()
			.stream()
			.filter(this::isProcessModel)
			.toList();
	}

	private Model getTenantModel(String modelId) {
		if (!StringUtils.hasText(modelId)) {
			throw new ServiceException("模型ID不能为空");
		}
		Model model = repositoryService.createModelQuery()
			.modelTenantId(currentTenantId())
			.modelId(modelId.trim())
			.singleResult();
		if (model == null || !isProcessModel(model)) {
			throw new ServiceException("流程模型不存在或无权访问");
		}
		return model;
	}

	private boolean isProcessModel(Model model) {
		return !FORM_MODEL_CATEGORY.equals(model.getCategory())
			&& !processDesignHistoryService.isHistoryCategory(model.getCategory());
	}

	private void validateModelKey(String modelKey, String modelId, String tenantId) {
		if (!MODEL_KEY_PATTERN.matcher(modelKey).matches()) {
			throw new ServiceException("模型key必须以字母或下划线开头，且只能包含字母、数字、点、横线和下划线");
		}
		boolean duplicated = repositoryService.createModelQuery()
			.modelTenantId(tenantId)
			.modelKey(modelKey)
			.list()
			.stream()
			.anyMatch(model -> !model.getId().equals(modelId));
		if (duplicated) {
			throw new ServiceException("当前租户已存在相同模型key");
		}
	}

	private void validateForm(String formType, String formKey) {
		if (!FORM_TYPES.contains(formType)) {
			throw new ServiceException("不支持的表单类型");
		}
		if (!"independent".equals(formType) && !StringUtils.hasText(formKey)) {
			throw new ServiceException("内置表单和外置表单必须填写表单key");
		}
	}

	private String resolveBpmnXml(ProcessModelDesignDTO processModelDesignDTO) {
		if (StringUtils.hasText(processModelDesignDTO.getBpmnXmlBase64())) {
			try {
				byte[] bytes = Base64.getDecoder().decode(processModelDesignDTO.getBpmnXmlBase64());
				return normalizeBpmnXml(new String(bytes, StandardCharsets.UTF_8));
			} catch (IllegalArgumentException exception) {
				throw new ServiceException("BPMN XML编码无效");
			}
		}
		return normalizeBpmnXml(processModelDesignDTO.getBpmnXml());
	}

	private String normalizeBpmnXml(String bpmnXml) {
		if (!StringUtils.hasText(bpmnXml)) {
			throw new ServiceException("BPMN XML不能为空");
		}
		String normalizedXml = bpmnXml.startsWith("\uFEFF") ? bpmnXml.substring(1) : bpmnXml;
		normalizedXml = normalizedXml.stripLeading();
		if (!normalizedXml.startsWith("<?xml") && !normalizedXml.startsWith("<bpmn:definitions") &&
			!normalizedXml.startsWith("<definitions")) {
			throw new ServiceException("BPMN XML内容已损坏，请重新进入流程设计并保存");
		}
		return normalizedXml;
	}

	private String synchronizeProcessMetadata(String bpmnXml, Model model) {
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			Document document = documentBuilderFactory.newDocumentBuilder()
				.parse(new InputSource(new StringReader(bpmnXml)));
			Element definitions = document.getDocumentElement();
			definitions.setAttribute(
				"targetNamespace",
				StringUtils.hasText(model.getCategory()) ? model.getCategory().trim() : DEFAULT_TARGET_NAMESPACE
			);

			Element process = findPrimaryProcess(document);
			String previousProcessId = process.getAttribute("id");
			process.setAttribute("id", model.getKey());
			process.setAttribute("name", model.getName());
			synchronizeRuntimeFormMetadata(document, definitions, process, model);
			synchronizeProcessReferences(document, previousProcessId, model.getKey());

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, StandardCharsets.UTF_8.name());
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			StringWriter stringWriter = new StringWriter();
			transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
			return normalizeBpmnXml(stringWriter.toString());
		} catch (Exception exception) {
			log.error("同步流程模型元数据失败，模型ID：{}", model.getId(), exception);
			throw new ServiceException("BPMN XML元数据同步失败，请重新进入流程设计并保存");
		}
	}

	private void synchronizeRuntimeFormMetadata(Document document, Element definitions, Element process, Model model) {
		ModelMetaInfo metaInfo = parseMetaInfo(model.getMetaInfo());
		definitions.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:blade", BLADE_NAMESPACE);
		upsertProcessProperty(document, process, "formType", trimToNull(metaInfo.getFormType()));
		upsertProcessProperty(document, process, "formKey", trimToNull(metaInfo.getFormKey()));
		upsertProcessProperty(document, process, "formFields", JsonUtil.toJson(metaInfo.getFormFields()));
	}

	private void upsertProcessProperty(Document document, Element process, String name, String value) {
		Element extensionElements = findDirectChild(process, BPMN_NAMESPACE, "extensionElements");
		Element existingProperty = null;
		if (extensionElements != null) {
			NodeList children = extensionElements.getChildNodes();
			for (int index = 0; index < children.getLength(); index++) {
				Node child = children.item(index);
				if (child instanceof Element property
					&& BLADE_NAMESPACE.equals(property.getNamespaceURI())
					&& "property".equals(property.getLocalName())
					&& name.equals(property.getAttribute("name"))) {
					existingProperty = property;
					break;
				}
			}
		}
		if (!StringUtils.hasText(value)) {
			if (existingProperty != null) {
				extensionElements.removeChild(existingProperty);
			}
			return;
		}
		if (extensionElements == null) {
			String prefix = process.getPrefix();
			String qualifiedName = StringUtils.hasText(prefix)
				? prefix + ":extensionElements"
				: "extensionElements";
			extensionElements = document.createElementNS(BPMN_NAMESPACE, qualifiedName);
			process.insertBefore(extensionElements, process.getFirstChild());
		}
		Element property = existingProperty;
		if (property == null) {
			property = document.createElementNS(BLADE_NAMESPACE, "blade:property");
			extensionElements.appendChild(property);
		}
		property.setAttribute("name", name);
		property.setAttribute("value", value);
	}

	private Element findDirectChild(Element owner, String namespace, String localName) {
		NodeList children = owner.getChildNodes();
		for (int index = 0; index < children.getLength(); index++) {
			Node child = children.item(index);
			if (child instanceof Element element
				&& localName.equals(element.getLocalName())
				&& (namespace.equals(element.getNamespaceURI()) || element.getNamespaceURI() == null)) {
				return element;
			}
		}
		return null;
	}

	private Element findPrimaryProcess(Document document) {
		NodeList processNodes = document.getElementsByTagNameNS(BPMN_NAMESPACE, "process");
		if (processNodes.getLength() == 0) {
			processNodes = document.getElementsByTagName("process");
		}
		if (processNodes.getLength() == 0) {
			throw new ServiceException("BPMN XML中未找到流程定义");
		}
		for (int index = 0; index < processNodes.getLength(); index++) {
			Element process = (Element) processNodes.item(index);
			if ("true".equalsIgnoreCase(process.getAttribute("isExecutable"))) {
				return process;
			}
		}
		return (Element) processNodes.item(0);
	}

	private void synchronizeProcessReferences(Document document, String previousProcessId, String processId) {
		if (!StringUtils.hasText(previousProcessId) || previousProcessId.equals(processId)) {
			return;
		}
		NodeList elementNodes = document.getElementsByTagName("*");
		for (int index = 0; index < elementNodes.getLength(); index++) {
			NamedNodeMap attributes = elementNodes.item(index).getAttributes();
			for (int attributeIndex = 0; attributeIndex < attributes.getLength(); attributeIndex++) {
				Node attribute = attributes.item(attributeIndex);
				String attributeName = attribute.getLocalName();
				if (("bpmnElement".equals(attributeName) || "processRef".equals(attributeName)) &&
					previousProcessId.equals(attribute.getNodeValue())) {
					attribute.setNodeValue(processId);
				}
			}
		}
	}

	private List<ProcessFormFieldDTO> normalizeFormFields(List<ProcessFormFieldDTO> formFields) {
		if (formFields == null) {
			return new ArrayList<>();
		}
		return formFields.stream()
			.filter(formField -> StringUtils.hasText(formField.getLabel()) || StringUtils.hasText(formField.getProp()))
			.peek(formField -> {
				formField.setLabel(trimToNull(formField.getLabel()));
				formField.setProp(trimToNull(formField.getProp()));
				formField.setReadable(Boolean.TRUE.equals(formField.getReadable()));
				formField.setWritable(Boolean.TRUE.equals(formField.getWritable()));
			})
			.toList();
	}

	private String serializeDesignMeta(ModelMetaInfo modelMetaInfo) {
		ModelDesignMetaInfo designMetaInfo = new ModelDesignMetaInfo();
		designMetaInfo.setFormType(modelMetaInfo.getFormType());
		designMetaInfo.setFormKey(modelMetaInfo.getFormKey());
		designMetaInfo.setFormFields(modelMetaInfo.getFormFields());
		return JsonUtil.toJson(designMetaInfo);
	}

	private void applyDesignMeta(ModelMetaInfo modelMetaInfo, String designMeta) {
		if (!StringUtils.hasText(designMeta)) {
			return;
		}
		try {
			ModelDesignMetaInfo designMetaInfo = JsonUtil.parse(designMeta, ModelDesignMetaInfo.class);
			if (designMetaInfo == null) {
				return;
			}
			modelMetaInfo.setFormType(designMetaInfo.getFormType());
			modelMetaInfo.setFormKey(designMetaInfo.getFormKey());
			modelMetaInfo.setFormFields(designMetaInfo.getFormFields() == null
				? new ArrayList<>()
				: designMetaInfo.getFormFields());
		} catch (Exception exception) {
			throw new ServiceException("模型历史版本的表单配置已损坏");
		}
	}

	private ProcessModelVO buildModelVO(Model model, boolean includeEditorSource) {
		ModelMetaInfo modelMetaInfo = parseMetaInfo(model.getMetaInfo());
		ProcessModelVO processModelVO = new ProcessModelVO();
		processModelVO.setId(model.getId());
		processModelVO.setKey(model.getKey());
		processModelVO.setName(model.getName());
		processModelVO.setCategory(model.getCategory());
		processModelVO.setDescription(modelMetaInfo.getDescription());
		processModelVO.setVersion(model.getVersion());
		processModelVO.setDeploymentId(model.getDeploymentId());
		processModelVO.setTenantId(model.getTenantId());
		processModelVO.setCreateTime(model.getCreateTime());
		processModelVO.setLastUpdateTime(model.getLastUpdateTime());
		processModelVO.setDesigned(model.hasEditorSource());
		processModelVO.setFormType(modelMetaInfo.getFormType());
		processModelVO.setFormKey(modelMetaInfo.getFormKey());
		processModelVO.setFormFields(modelMetaInfo.getFormFields());
		if (includeEditorSource) {
			byte[] editorSource = repositoryService.getModelEditorSource(model.getId());
			if (editorSource != null && editorSource.length > 0) {
				processModelVO.setDesigned(true);
				processModelVO.setBpmnXml(new String(editorSource, StandardCharsets.UTF_8));
			}
		}
		return processModelVO;
	}

	private ModelMetaInfo parseMetaInfo(String metaInfo) {
		if (!StringUtils.hasText(metaInfo)) {
			return new ModelMetaInfo();
		}
		try {
			ModelMetaInfo modelMetaInfo = JsonUtil.parse(metaInfo, ModelMetaInfo.class);
			if (modelMetaInfo == null) {
				return new ModelMetaInfo();
			}
			if (modelMetaInfo.getFormFields() == null) {
				modelMetaInfo.setFormFields(new ArrayList<>());
			}
			return modelMetaInfo;
		} catch (Exception exception) {
			log.warn("解析流程模型元数据失败，将使用空元数据，内容：{}", metaInfo, exception);
			return new ModelMetaInfo();
		}
	}

	private String trimToNull(String value) {
		return StringUtils.hasText(value) ? value.trim() : null;
	}

	private boolean containsIgnoreCase(String source, String keyword) {
		return source != null && source.toLowerCase().contains(keyword.trim().toLowerCase());
	}

	private String currentTenantId() {
		String tenantId = SecureUtil.getTenantId();
		if (!StringUtils.hasText(tenantId)) {
			throw new ServiceException("无法获取当前租户信息");
		}
		return tenantId;
	}

	@Data
	private static final class ModelMetaInfo {

		private String description;
		private String formType;
		private String formKey;
		private List<ProcessFormFieldDTO> formFields = new ArrayList<>();

	}

	@Data
	private static final class ModelDesignMetaInfo {

		private String formType;
		private String formKey;
		private List<ProcessFormFieldDTO> formFields = new ArrayList<>();

	}

}
