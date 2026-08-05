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
package org.springblade.procurement.reviewexpert.pojo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import java.io.Serializable;
import org.springblade.core.mp.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springblade.core.tool.jackson.BladeView;
import org.springblade.core.tool.jackson.Views;
import org.springblade.core.tool.utils.DateUtil;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;


/**
 * 评标专家库实体类
 *
 * @author Chill
 * @since 2026-07-23
 */
@Data
@TableName(value = "proc_review_expert", autoResultMap = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "评标专家库")
public class ReviewExpert implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@Schema(description = "主键")
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

//	/**
//	 * 创建人
//	 */
//	@BladeView(Views.Admin.class)
//	@JsonSerialize(using = ToStringSerializer.class)
//	@Schema(description = "创建人", hidden = true)
//	private Long createUser;

//	/**
//	 * 创建部门
//	 */
//	@BladeView(Views.Admin.class)
//	@JsonSerialize(using = ToStringSerializer.class)
//	@Schema(description = "创建部门", hidden = true)
//	private Long createDept;

	/**
	 * 创建时间
	 */
	@BladeView(Views.Detail.class)
	@DateTimeFormat(pattern = DateUtil.PATTERN_DATETIME)
	@JsonFormat(pattern = DateUtil.PATTERN_DATETIME)
	@Schema(description = "创建时间", hidden = true)
	private Date createTime;

//	/**
//	 * 更新人
//	 */
//	@BladeView(Views.Admin.class)
//	@JsonSerialize(using = ToStringSerializer.class)
//	@Schema(description = "更新人", hidden = true)
//	private Long updateUser;

	/**
	 * 更新时间
	 */
	@BladeView(Views.Detail.class)
	@DateTimeFormat(pattern = DateUtil.PATTERN_DATETIME)
	@JsonFormat(pattern = DateUtil.PATTERN_DATETIME)
	@Schema(description = "更新时间", hidden = true)
	private Date updateTime;

//	/**
//	 * 状态[1:正常]
//	 */
//	@BladeView(Views.Detail.class)
//	@Schema(description = "业务状态", hidden = true)
//	private Integer status;

//	/**
//	 * 状态[0:未删除,1:删除]
//	 */
//	@BladeView(Views.Admin.class)
//	@TableLogic
//	@Schema(description = "是否已删除", hidden = true)
//	private Integer isDeleted;

	/**
	 * 专家账号
	 */
  	@Schema(description = "专家账号")
  	private String expertCode;
	/**
	 * 专家姓名
	 */
  	@Schema(description = "专家姓名")
  	private String expertName;
	/**
	 * 专家来源类型，字典：proc_review_expert_source
	 */
  	@Schema(description = "专家来源类型，字典：proc_review_expert_source")
  	private String expertSourceType;
	/**
	 * 性别，字典：sys_user_sex
	 */
  	@Schema(description = "性别，字典：sys_user_sex")
  	private String sex;
	/**
	 * 手机号
	 */
  	@Schema(description = "手机号")
  	private String phone;
	/**
	 * 本专业年限
	 */
  	@Schema(description = "本专业年限")
  	private String majorYears;
	/**
	 * 专业标签编码集合
	 */
	@Schema(description = "专业标签编码集合")
	@TableField(typeHandler = JacksonTypeHandler.class)
	private List<String> professionalCodes;
	/**
	 * 部门编号
	 */
  	@Schema(description = "部门编号")
  	private String deptCode;
	/**
	 * 部门名称
	 */
  	@Schema(description = "部门名称")
  	private String deptName;
	/**
	 * 科室编号
	 */
  	@Schema(description = "科室编号")
  	private String departmentCode;
	/**
	 * 科室名称
	 */
  	@Schema(description = "科室名称")
  	private String departmentName;
	/**
	 * 审批状态：0未审批，1准入审批中，2准入驳回，3已准入，4清退审批中，5清退驳回，6已清退
	 */
  	@Schema(description = "审批状态：0未审批，1准入审批中，2准入驳回，3已准入，4清退审批中，5清退驳回，6已清退")
  	private String approvalStatus;
	/**
	 * 准入说明
	 */
  	@Schema(description = "准入说明")
  	private String admissionDescription;
	/**
	 * 清退说明
	 */
  	@Schema(description = "清退说明")
  	private String retirementDescription;
	/**
	 * 部门一把手账号
	 */
  	@Schema(description = "部门一把手账号")
  	private String deptLeaderBy;
	/**
	 * 部门一把手名称
	 */
  	@Schema(description = "部门一把手名称")
  	private String deptLeaderName;
	/**
	 * 人力资源审批人账号
	 */
  	@Schema(description = "人力资源审批人账号")
  	private String hrApproverBy;
	/**
	 * 人力资源审批人名称
	 */
  	@Schema(description = "人力资源审批人名称")
  	private String hrApproverName;
	/**
	 * 启用状态：Y启用，N停用
	 */
  	@Schema(description = "启用状态：Y启用，N停用")
  	private String enableStatus;
	/**
	 * 预留字段1
	 */
  	@Schema(description = "预留字段1")
  	private String fields1;
	/**
	 * 预留字段2
	 */
  	@Schema(description = "预留字段2")
  	private String fields2;
	/**
	 * 创建人账号
	 */
  	@Schema(description = "创建人账号")
  	private String createBy;
	/**
	 * 创建人昵称
	 */
  	@Schema(description = "创建人昵称")
  	private String createName;
	/**
	 * 更新人账号
	 */
  	@Schema(description = "更新人账号")
  	private String updateBy;
	/**
	 * 更新人昵称
	 */
  	@Schema(description = "更新人昵称")
  	private String updateName;
	/**
	 * 备注
	 */
  	@Schema(description = "备注")
  	private String remark;
	/**
	 * 租户编号
	 */
  	@Schema(description = "租户编号")
  	private String tenantCode;
	/**
	 * 删除标记：0正常，1删除
	 */
  	@Schema(description = "删除标记：0正常，1删除")
	@TableLogic(value = "0", delval = "1")
  	private String delFlag;


}

