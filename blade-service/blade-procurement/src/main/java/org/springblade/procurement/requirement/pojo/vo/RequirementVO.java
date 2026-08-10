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
package org.springblade.procurement.requirement.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springblade.procurement.requirement.pojo.entity.BiddingPartsDetail;
import org.springblade.procurement.requirement.pojo.entity.BiddingScrapDetail;
import org.springblade.procurement.requirement.pojo.entity.BiddingTrialDetail;
import org.springblade.procurement.requirement.pojo.entity.PurchaseRequirementDetail;
import org.springblade.procurement.requirement.pojo.entity.Requirement;
import org.springblade.procurement.requirement.pojo.entity.RequirementFile;

import java.util.List;

/** 采购与竞价需求详情。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RequirementVO extends Requirement {
	private List<PurchaseRequirementDetail> purchaseDetails;
	private List<BiddingTrialDetail> biddingTrialDetails;
	private List<BiddingScrapDetail> biddingScrapDetails;
	private List<BiddingPartsDetail> biddingPartsDetails;
	private List<RequirementFile> files;
}
