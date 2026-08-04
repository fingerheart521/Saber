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

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 流程定义视图对象
 *
 * @author Chill
 */
@Data
@Schema(description = "流程定义")
public class ProcessDefinitionVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "流程定义ID")
	private String id;

	@Schema(description = "流程定义标识")
	private String key;

	@Schema(description = "流程定义名称")
	private String name;

	@Schema(description = "流程分类")
	private String category;

	@Schema(description = "流程版本")
	private Integer version;

	@Schema(description = "部署ID")
	private String deploymentId;

	@Schema(description = "BPMN资源名称")
	private String resourceName;

	@Schema(description = "租户编号")
	private String tenantId;

	@Schema(description = "是否挂起")
	private Boolean suspended;

	@Schema(description = "部署时间")
	private Date deploymentTime;

}
