/**
 * Copyright (c) 2018-2099, Chill Zhuang 庄骞 (bladejava@qq.com).
 * Modifications Copyright (c) 2026, fingerheart521 (daoguangliu@qq.com).
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
package org.springblade.procurement.requirement.pojo.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/** 需求处理时更新包号的参数。 */
@Data
public class RequirementPackageDTO implements Serializable {
	@Serial private static final long serialVersionUID = 1L;
	private Long requirementId;
	private String projectName;
	private String type;
	private String procurementEngineerBy;
	private String procurementEngineerName;
	private String projectLeaderBy;
	private String projectLeaderName;
	private String purchaseMethod;
	private String projectType;
	private String remark;
	private List<PackageItem> items;

	@Data
	public static class PackageItem implements Serializable {
		@Serial private static final long serialVersionUID = 1L;
		private Long requirementId;
		private String detailType;
		private Long detailId;
		private String packageNo;
	}
}
