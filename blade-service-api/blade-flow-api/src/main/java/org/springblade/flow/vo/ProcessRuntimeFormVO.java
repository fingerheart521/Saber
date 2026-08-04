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
package org.springblade.flow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springblade.flow.dto.ProcessFormFieldDTO;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 流程运行时表单视图对象
 *
 * @author Chill
 */
@Data
@Schema(description = "流程运行时表单")
public class ProcessRuntimeFormVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "表单类型")
	private String formType;

	@Schema(description = "表单key")
	private String formKey;

	@Schema(description = "表单名称")
	private String formName;

	@Schema(description = "表单设计JSON")
	private String formJson;

	@Schema(description = "当前节点字段读写权限")
	private List<ProcessFormFieldDTO> fields;

	@Schema(description = "当前流程变量值")
	private Map<String, Object> values;

}
