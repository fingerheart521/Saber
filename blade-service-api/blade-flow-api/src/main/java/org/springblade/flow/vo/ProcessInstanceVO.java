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
 * 流程实例视图对象
 *
 * @author Chill
 */
@Data
@Schema(description = "流程实例")
public class ProcessInstanceVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "流程实例ID")
	private String id;

	@Schema(description = "流程定义ID")
	private String processDefinitionId;

	@Schema(description = "流程定义标识")
	private String processDefinitionKey;

	@Schema(description = "流程定义名称")
	private String processDefinitionName;

	@Schema(description = "流程实例标题")
	private String title;

	@Schema(description = "流程流水号")
	private String serialNumber;

	@Schema(description = "业务标识")
	private String businessKey;

	@Schema(description = "发起人ID")
	private String startUserId;

	@Schema(description = "开始时间")
	private Date startTime;

	@Schema(description = "结束时间")
	private Date endTime;

	@Schema(description = "是否挂起")
	private Boolean suspended;

	@Schema(description = "状态：RUNNING、SUSPENDED、COMPLETED、CANCELED")
	private String state;

	@Schema(description = "撤销原因")
	private String deleteReason;

	@Schema(description = "租户编号")
	private String tenantId;

}
