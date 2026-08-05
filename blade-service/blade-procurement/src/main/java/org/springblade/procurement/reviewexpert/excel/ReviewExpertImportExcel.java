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
package org.springblade.procurement.reviewexpert.excel;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 评标专家导入模型。
 *
 * @author Chill
 * @since 2026-07-24
 */
@Data
public class ReviewExpertImportExcel implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	@ExcelProperty("专家账号")
	private String expertCode;

	@ExcelProperty("专家姓名")
	private String expertName;

	@ExcelProperty("专家来源")
	private String expertSourceType;

	@ExcelProperty("性别")
	private String sex;

	@ExcelProperty("手机号")
	private String phone;

	@ExcelProperty("本专业年限")
	private String majorYears;

	@ExcelProperty("专业编码")
	private String professionalCodes;

	@ExcelProperty("部门编码")
	private String deptCode;

	@ExcelProperty("部门名称")
	private String deptName;

	@ExcelProperty("科室编码")
	private String departmentCode;

	@ExcelProperty("科室名称")
	private String departmentName;

	@ExcelProperty("备注")
	private String remark;
}

