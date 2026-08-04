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
import java.util.List;
import java.util.Map;

/**
 * 流程任务视图对象
 *
 * @author Chill
 */
@Data
@Schema(description = "流程任务")
public class ProcessTaskVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "任务ID")
	private String id;

	@Schema(description = "任务名称")
	private String name;

	@Schema(description = "任务定义标识")
	private String taskDefinitionKey;

	@Schema(description = "流程实例ID")
	private String processInstanceId;

	@Schema(description = "流程定义ID")
	private String processDefinitionId;

	@Schema(description = "业务标识")
	private String businessKey;

	@Schema(description = "办理人")
	private String assignee;

	@Schema(description = "创建时间")
	private Date createTime;

	@Schema(description = "完成时间")
	private Date endTime;

	@Schema(description = "到期时间")
	private Date dueDate;

	@Schema(description = "租户编号")
	private String tenantId;

	@Schema(description = "当前节点启用的流程按钮")
	private List<String> availableButtons;

	@Schema(description = "可指定回退的历史节点")
	private List<ProcessNodeVO> rollbackTargets;

	@Schema(description = "节点表单与界面扩展配置")
	private Map<String, String> nodeProperties;

}
