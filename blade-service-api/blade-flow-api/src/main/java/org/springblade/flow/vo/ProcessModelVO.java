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
import java.util.Date;
import java.util.List;

/**
 * 流程模型视图对象
 *
 * @author Chill
 */
@Data
@Schema(description = "流程模型")
public class ProcessModelVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "模型ID")
	private String id;

	@Schema(description = "模型key")
	private String key;

	@Schema(description = "模型名称")
	private String name;

	@Schema(description = "模型分类")
	private String category;

	@Schema(description = "模型描述")
	private String description;

	@Schema(description = "模型版本")
	private Integer version;

	@Schema(description = "部署ID")
	private String deploymentId;

	@Schema(description = "租户编号")
	private String tenantId;

	@Schema(description = "创建时间")
	private Date createTime;

	@Schema(description = "最后更新时间")
	private Date lastUpdateTime;

	@Schema(description = "是否已有流程设计")
	private Boolean designed;

	@Schema(description = "表单类型")
	private String formType;

	@Schema(description = "表单key")
	private String formKey;

	@Schema(description = "表单字段读写权限")
	private List<ProcessFormFieldDTO> formFields;

	@Schema(description = "BPMN XML，仅详情接口返回")
	private String bpmnXml;

}
