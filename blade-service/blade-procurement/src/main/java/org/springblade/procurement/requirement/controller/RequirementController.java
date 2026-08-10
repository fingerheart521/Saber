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
package org.springblade.procurement.requirement.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.PreAuth;
import org.springblade.core.tool.constant.RoleConstant;
import org.springblade.core.swagger.annotation.ApiOrder;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.utils.Func;
import org.springblade.procurement.requirement.pojo.dto.RequirementDTO;
import org.springblade.procurement.requirement.pojo.dto.RequirementPackageDTO;
import org.springblade.procurement.requirement.pojo.entity.Requirement;
import org.springblade.procurement.requirement.excel.RequirementExportExcel;
import org.springblade.procurement.requirement.pojo.vo.RequirementVO;
import org.springblade.procurement.requirement.service.IRequirementService;
import org.springblade.flow.vo.ProcessTaskVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

/**
 * 采购与竞价需求控制器。
 *
 * @author fingerheart521
 */
@RestController
@AllArgsConstructor
@RequestMapping("/requirement")
@ApiOrder
@Tag(name = "采购与竞价需求", description = "采购与竞价需求接口")
public class RequirementController extends BladeController {

	private IRequirementService requirementService;

	@GetMapping("/page")
	@Operation(summary = "需求分页")
	public R<IPage<Requirement>> page(@RequestParam Map<String, Object> params,
		@RequestParam(defaultValue = "purchase") String type, Query query) {
		params.remove("type");
		return R.data(requirementService.page(Condition.getPage(query), params, type));
	}

	@GetMapping("/detail")
	@Operation(summary = "需求详情")
	public R<RequirementVO> detail(@RequestParam Long id) {
		return R.data(requirementService.detail(id));
	}

	@PostMapping("/submit")
	@Operation(summary = "保存采购需求或补充竞价明细")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R submit(@Valid @RequestBody RequirementDTO dto) {
		return R.status(requirementService.submit(dto));
	}

	@PostMapping("/oa-receive")
	@Operation(summary = "模拟OA传入竞价需求并保存草稿")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R receiveFromOa(@Valid @RequestBody RequirementDTO dto) {
		return R.status(requirementService.receiveFromOa(dto));
	}

	@PostMapping("/oa-receive-review")
	@Operation(summary = "模拟OA传入竞价需求并提交审核")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R receiveFromOaReview(@Valid @RequestBody RequirementDTO dto) {
		return R.status(requirementService.receiveFromOaReview(dto));
	}

	@PostMapping("/process")
	@Operation(summary = "需求处理，保存明细包号")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R process(@Valid @RequestBody RequirementPackageDTO dto) {
		return R.status(requirementService.process(dto));
	}

	@PostMapping("/submit-review")
	@Operation(summary = "提交审核")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R submitReview(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(requirementService.submitReview(Func.toLongList(ids)));
	}

	@PostMapping("/prepare-approval")
	@Operation(summary = "准备采购需求审批流程")
	public R prepareApproval(@RequestParam Long id) {
		return R.status(requirementService.prepareApproval(id));
	}

	@GetMapping("/approval-task")
	@Operation(summary = "查询采购需求审批任务")
	public R<ProcessTaskVO> approvalTask(@RequestParam Long id) {
		return R.data(requirementService.currentApprovalTask(id));
	}

	@PostMapping("/approve")
	@Operation(summary = "审批通过采购需求")
	public R approve(@RequestParam Long id, @RequestParam(required = false) String comment) {
		return R.status(requirementService.approve(id, comment));
	}

	@PostMapping("/reject")
	@Operation(summary = "驳回采购需求")
	public R reject(@RequestParam Long id, @RequestParam(required = false) String comment) {
		return R.status(requirementService.reject(id, comment));
	}

	@PostMapping("/cancel")
	@Operation(summary = "取消处理")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R cancel(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(requirementService.cancel(Func.toLongList(ids)));
	}

	@GetMapping("/export")
	@Operation(summary = "导出需求")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public void export(@RequestParam Map<String, Object> params,
		@RequestParam(defaultValue = "purchase") String type,
		HttpServletResponse response) {
		params.remove("type");
		params.remove("scope");
		List<RequirementExportExcel> list = requirementService.export(params, type);
		ExcelUtil.export(response,
			"采购与竞价需求",
			"需求数据",
			list,
			RequirementExportExcel.class);
	}

	@PostMapping("/remove")
	@Operation(summary = "逻辑删除需求")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(requirementService.deleteLogic(Func.toLongList(ids)));
	}
}
