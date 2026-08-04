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
 * 流程设计复制参数
 *
 * @author Chill
 */
@Data
@Schema(description = "流程设计复制参数")
public class ProcessDesignCopyDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "源数据ID不能为空")
	@Schema(description = "源模型或表单ID", requiredMode = Schema.RequiredMode.REQUIRED)
	private String sourceId;

	@NotBlank(message = "复制后的key不能为空")
	@Size(max = 100, message = "复制后的key不能超过100个字符")
	@Schema(description = "复制后的key", requiredMode = Schema.RequiredMode.REQUIRED)
	private String key;

	@NotBlank(message = "复制后的名称不能为空")
	@Size(max = 200, message = "复制后的名称不能超过200个字符")
	@Schema(description = "复制后的名称", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

}
