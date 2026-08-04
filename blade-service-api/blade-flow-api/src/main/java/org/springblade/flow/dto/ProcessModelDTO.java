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
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 流程模型参数
 *
 * @author Chill
 */
@Data
@Schema(description = "流程模型参数")
public class ProcessModelDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "模型ID，新增时为空")
	private String id;

	@NotBlank(message = "模型key不能为空")
	@Size(max = 100, message = "模型key不能超过100个字符")
	@Schema(description = "模型key", requiredMode = Schema.RequiredMode.REQUIRED)
	private String key;

	@NotBlank(message = "模型名称不能为空")
	@Size(max = 200, message = "模型名称不能超过200个字符")
	@Schema(description = "模型名称", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

	@Size(max = 100, message = "模型分类不能超过100个字符")
	@Schema(description = "模型分类")
	private String category;

	@Size(max = 1000, message = "模型描述不能超过1000个字符")
	@Schema(description = "模型描述")
	private String description;

}
