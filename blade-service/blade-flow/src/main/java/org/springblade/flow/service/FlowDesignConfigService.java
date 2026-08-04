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
import lombok.RequiredArgsConstructor;
import org.flowable.engine.RepositoryService;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.tool.jackson.JsonUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程设计器扩展属性读取服务
 *
 * @author Chill
 */
@Service
@RequiredArgsConstructor
public class FlowDesignConfigService {

	private static final String BLADE_NAMESPACE = "https://springblade.org/schema/bpmn";
	private static final String FLOWABLE_NAMESPACE = "http://flowable.org/bpmn";
	private final RepositoryService repositoryService;
	private final Map<String, FlowDesignConfig> cache = new ConcurrentHashMap<>();

	public FlowDesignConfig getConfig(String processDefinitionId) {
		if (!StringUtils.hasText(processDefinitionId)) {
			return FlowDesignConfig.empty();
		}
		return cache.computeIfAbsent(processDefinitionId, this::loadConfig);
	}

	public String processProperty(String processDefinitionId, String name) {
		return getConfig(processDefinitionId).getProcessProperties().get(name);
	}

	public String elementProperty(String processDefinitionId, String elementId, String name) {
		return getConfig(processDefinitionId).getElementProperties()
			.getOrDefault(elementId, Collections.emptyMap())
			.get(name);
	}

	public List<String> taskButtons(String processDefinitionId, String elementId) {
		String json = elementProperty(processDefinitionId, elementId, "buttons");
		if (!StringUtils.hasText(json)) {
			return List.of("wf_pass", "wf_reject", "wf_transfer", "wf_rollback");
		}
		try {
			List<?> values = JsonUtil.parse(json, List.class);
			if (values == null) {
				return Collections.emptyList();
			}
			return values.stream().map(String::valueOf).filter(StringUtils::hasText).toList();
		} catch (Exception exception) {
			throw new ServiceException("流程按钮配置无法解析");
		}
	}

	public SerialConfig serialConfig(String processDefinitionId) {
		String json = processProperty(processDefinitionId, "serialConfig");
		if (!StringUtils.hasText(json)) {
			return new SerialConfig();
		}
		try {
			SerialConfig config = JsonUtil.parse(json, SerialConfig.class);
			return config == null ? new SerialConfig() : config;
		} catch (Exception exception) {
			throw new ServiceException("流程流水号配置无法解析");
		}
	}

	private FlowDesignConfig loadConfig(String processDefinitionId) {
		try (InputStream inputStream = repositoryService.getProcessModel(processDefinitionId)) {
			if (inputStream == null) {
				return FlowDesignConfig.empty();
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
			Document document = factory.newDocumentBuilder().parse(inputStream);
			Map<String, String> processProperties = new LinkedHashMap<>();
			Map<String, Map<String, String>> elementProperties = new HashMap<>();
			Deque<Element> elements = new ArrayDeque<>();
			elements.add(document.getDocumentElement());
			while (!elements.isEmpty()) {
				Element element = elements.removeFirst();
				String id = element.getAttribute("id");
				Map<String, String> properties = readDirectProperties(element);
				if (!properties.isEmpty()) {
					if ("process".equals(element.getLocalName())) {
						processProperties.putAll(properties);
					}
					if (StringUtils.hasText(id)) {
						elementProperties.put(id, properties);
					}
				}
				if ("process".equals(element.getLocalName())) {
					putLegacyProcessProperty(processProperties, element, "skipFirstNode", "skipFirstNode");
					putLegacyProcessProperty(processProperties, element, "defaultRejectTarget", "rollbackNode");
				}
				NodeList children = element.getChildNodes();
				for (int index = 0; index < children.getLength(); index++) {
					Node child = children.item(index);
					if (child instanceof Element childElement) {
						elements.addLast(childElement);
					}
				}
			}
			return new FlowDesignConfig(processProperties, elementProperties);
		} catch (Exception exception) {
			throw new ServiceException("读取流程设计配置失败: " + exception.getMessage());
		}
	}

	private void putLegacyProcessProperty(Map<String, String> properties, Element process,
									  String propertyName, String attributeName) {
		if (properties.containsKey(propertyName)) {
			return;
		}
		String value = process.getAttributeNS(FLOWABLE_NAMESPACE, attributeName);
		if (StringUtils.hasText(value)) {
			properties.put(propertyName, value);
		}
	}

	private Map<String, String> readDirectProperties(Element owner) {
		Map<String, String> properties = new LinkedHashMap<>();
		NodeList children = owner.getChildNodes();
		for (int index = 0; index < children.getLength(); index++) {
			Node child = children.item(index);
			if (!(child instanceof Element extensionElements)
				|| !"extensionElements".equals(extensionElements.getLocalName())) {
				continue;
			}
			NodeList extensionChildren = extensionElements.getChildNodes();
			for (int extensionIndex = 0; extensionIndex < extensionChildren.getLength(); extensionIndex++) {
				Node extensionChild = extensionChildren.item(extensionIndex);
				if (extensionChild instanceof Element property
					&& BLADE_NAMESPACE.equals(property.getNamespaceURI())
					&& "property".equals(property.getLocalName())) {
					String name = property.getAttribute("name");
					if (StringUtils.hasText(name)) {
						properties.put(name, property.getAttribute("value"));
					}
				}
			}
		}
		return properties;
	}

	@Data
	@AllArgsConstructor
	public static class FlowDesignConfig {
		private Map<String, String> processProperties;
		private Map<String, Map<String, String>> elementProperties;

		private static FlowDesignConfig empty() {
			return new FlowDesignConfig(Collections.emptyMap(), Collections.emptyMap());
		}
	}

	@Data
	public static class SerialConfig {
		private boolean enabled;
		private String name;
		private String prefix;
		private String dateFormat = "yyyyMMdd";
		private int digits = 5;
		private long initial;
		private String connector = "";
		private String resetCycle = "none";
	}

}
