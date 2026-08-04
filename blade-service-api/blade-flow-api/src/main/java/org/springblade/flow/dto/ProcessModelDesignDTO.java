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
package org.springblade.flow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 流程模型设计参数
 *
 * @author Chill
 */
@Data
@Schema(description = "流程模型设计参数")
public class ProcessModelDesignDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "模型ID不能为空")
	@Schema(description = "模型ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String modelId;

	@NotBlank(message = "表单类型不能为空")
	@Schema(description = "表单类型：internal、external、independent", requiredMode = Schema.RequiredMode.REQUIRED)
	private String formType;

	@Schema(description = "表单key")
	private String formKey;

	@Schema(description = "表单字段读写权限")
	private List<ProcessFormFieldDTO> formFields;

	@Schema(description = "BPMN XML（兼容旧客户端）")
	private String bpmnXml;

	@Schema(description = "Base64编码的BPMN XML")
	private String bpmnXmlBase64;

}
