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
 * 流程历史视图对象
 *
 * @author Chill
 */
@Data
@Schema(description = "流程历史")
public class ProcessHistoryVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "活动ID")
	private String activityId;

	@Schema(description = "活动名称")
	private String activityName;

	@Schema(description = "活动类型")
	private String activityType;

	@Schema(description = "任务ID")
	private String taskId;

	@Schema(description = "办理人")
	private String assignee;

	@Schema(description = "开始时间")
	private Date startTime;

	@Schema(description = "结束时间")
	private Date endTime;

	@Schema(description = "持续时长，单位毫秒")
	private Long duration;

	@Schema(description = "审批意见")
	private String comment;

}
