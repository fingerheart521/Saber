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
 * 流程设计历史版本视图对象
 *
 * @author Chill
 */
@Data
@Schema(description = "流程设计历史版本")
public class ProcessDesignHistoryVO implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@Schema(description = "历史快照ID")
	private String id;

	@Schema(description = "业务数据ID")
	private String businessId;

	@Schema(description = "设计类型：model、form")
	private String designType;

	@Schema(description = "业务key")
	private String businessKey;

	@Schema(description = "业务名称")
	private String businessName;

	@Schema(description = "版本号")
	private Integer version;

	@Schema(description = "恢复来源版本，普通保存时为空")
	private Integer sourceVersion;

	@Schema(description = "是否当前主版本")
	private Boolean current;

	@Schema(description = "版本创建时间")
	private Date createTime;

	@Schema(description = "BPMN XML或Avue表单JSON，仅预览接口返回")
	private String designContent;

}
