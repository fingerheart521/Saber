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
package org.springblade.procurement.requirement.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import org.springblade.procurement.requirement.pojo.dto.RequirementDTO;
import org.springblade.procurement.requirement.pojo.dto.RequirementPackageDTO;
import org.springblade.procurement.requirement.pojo.entity.Requirement;
import org.springblade.procurement.requirement.pojo.vo.RequirementVO;

import java.util.List;
import java.util.Map;
import org.springblade.procurement.requirement.excel.RequirementExportExcel;
import org.springblade.flow.vo.ProcessTaskVO;

public interface IRequirementService extends IService<Requirement> {
	IPage<Requirement> page(IPage<Requirement> page, Map<String, Object> params, String type);
	RequirementVO detail(Long id);
	boolean submit(RequirementDTO dto);
	boolean receiveFromOa(RequirementDTO dto);
	boolean receiveFromOaReview(RequirementDTO dto);
	boolean process(RequirementPackageDTO dto);
	boolean submitReview(List<Long> ids);
	boolean prepareApproval(Long id);
	ProcessTaskVO currentApprovalTask(Long id);
	boolean approve(Long id, String comment);
	boolean reject(Long id, String comment);
	boolean cancel(List<Long> ids);
	boolean deleteLogic(List<Long> ids);
	List<RequirementExportExcel> export(Map<String, Object> params, String type);
}
