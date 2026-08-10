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
package org.springblade.procurement.requirement.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/** 折价配件类竞价明细。 */
@Data
@TableName("proc_bidding_parts_detail")
public class BiddingPartsDetail implements Serializable {
	@Serial private static final long serialVersionUID = 1L;
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;
	@JsonSerialize(using = ToStringSerializer.class)
	private Long requirementId;
	private String packageNo;
	private String itemCode;
	private String itemName;
	private String drawcode;
	private BigDecimal qty;
	private String unit;
	private String applicableModel;
	@TableField(exist = false)
	private List<RequirementFile> files;
	private String fields1;
	private String fields2;
	private String createBy;
	private String createName;
	private Date createTime;
	private String updateBy;
	private String updateName;
	private Date updateTime;
	private String remark;
	private String tenantCode;
	@TableLogic(value = "0", delval = "1")
	private String delFlag;
}
