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
import java.util.Map;

/**
 * 流程任务动作参数
 *
 * @author Chill
 */
@Data
@Schema(description = "流程任务动作参数")
public class TaskActionDTO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@NotBlank(message = "任务ID不能为空")
	private String taskId;

	@NotBlank(message = "任务动作不能为空")
	@Schema(description = "wf_pass、wf_reject、wf_transfer、wf_delegate、wf_terminate、wf_add_instance、wf_del_instance、wf_rollback、wf_draft、wf_withdraw")
	private String action;

	@Schema(description = "目标用户ID，转办、委托和加签时使用")
	private String targetUserId;

	@Schema(description = "目标节点ID，指定回退时使用")
	private String targetActivityId;

	@Schema(description = "审批意见")
	private String comment;

	@Schema(description = "流程变量")
	private Map<String, Object> variables;

}
