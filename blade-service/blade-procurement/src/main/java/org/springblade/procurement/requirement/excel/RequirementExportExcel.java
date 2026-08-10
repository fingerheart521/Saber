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
package org.springblade.procurement.requirement.excel;

import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.annotation.write.style.ColumnWidth;
import cn.idev.excel.annotation.write.style.ContentRowHeight;
import cn.idev.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/** 采购与竞价需求导出模型。 */
@Data
@ColumnWidth(20)
@HeadRowHeight(20)
@ContentRowHeight(18)
public class RequirementExportExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@ExcelProperty("需求编号")
	private String requirementCode;

	@ExcelProperty("需求名称")
	private String requirementName;

	@ExcelProperty("品类")
	private String categoryName;

	@ExcelProperty("金额（元）")
	private String amount;

	@ExcelProperty("需求状态")
	private String approvalStatus;

	@ExcelProperty("采购方式建议")
	private String purchaseMethodSuggestion;

	@ExcelProperty("招采工程师")
	private String procurementEngineerName;

	@ExcelProperty("项目负责人")
	private String projectLeaderName;

	@ExcelProperty("需求部门")
	private String requirementDeptName;

	@ExcelProperty("需求来源")
	private String requirementSource;

	@ExcelProperty("创建人")
	private String createName;

	@ExcelProperty("创建时间")
	private Date createTime;

	@ExcelProperty("更新人")
	private String updateName;

	@ExcelProperty("更新时间")
	private Date updateTime;
}
