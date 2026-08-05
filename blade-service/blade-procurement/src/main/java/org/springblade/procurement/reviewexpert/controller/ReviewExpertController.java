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
package org.springblade.procurement.reviewexpert.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;

import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.annotation.PreAuth;
import org.springblade.core.secure.utils.SecureUtil;
import org.springblade.core.swagger.annotation.ApiOrder;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.constant.RoleConstant;
import org.springblade.core.tool.utils.Func;
import org.springblade.procurement.reviewexpert.pojo.dto.ReviewExpertDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.procurement.reviewexpert.pojo.entity.ReviewExpert;
import org.springblade.procurement.reviewexpert.pojo.vo.ReviewExpertVO;
import org.springblade.procurement.reviewexpert.service.IReviewExpertService;
import org.springblade.core.boot.ctrl.BladeController;
import org.springblade.core.excel.util.ExcelUtil;
import org.springblade.procurement.reviewexpert.excel.ReviewExpertExportExcel;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

import cn.idev.excel.FastExcel;
import org.springblade.procurement.reviewexpert.excel.ReviewExpertImportExcel;
import org.springblade.procurement.reviewexpert.pojo.dto.ReviewExpertImportResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Locale;

/**
 * 评标专家库 控制器
 *
 * @author Chill
 * @since 2026-07-23
 */
@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/review-expert")
@ApiOrder
@Tag(name = "评标专家库", description = "评标专家库接口")
public class ReviewExpertController extends BladeController {

	private IReviewExpertService reviewExpertService;

	/**
	 * 详情
	 */
	@GetMapping("/detail")
	@Operation(summary = "详情", description = "传入reviewExpert")
	public R<ReviewExpertVO> detail(@RequestParam Long id) {
		return R.data(reviewExpertService.detail(id));
	}

	/**
	 * 分页 评标专家库
	 */
	@GetMapping("/list")
	public R<IPage<ReviewExpert>> list(
		@RequestParam Map<String, Object> params,
		Query query) {

		params.put("tenantCode_equal", SecureUtil.getTenantId());

		IPage<ReviewExpert> pages = reviewExpertService.page(
			Condition.getPage(query),
			Condition.getQueryWrapper(params, ReviewExpert.class)
		);

		return R.data(pages);
	}

	/**
	 * 自定义分页 评标专家库
	 */
	@GetMapping("/page")
	@Operation(summary = "分页", description = "传入reviewExpert")
	public R<IPage<ReviewExpertVO>> page(ReviewExpertVO reviewExpert, Query query) {
		reviewExpert.setTenantCode(SecureUtil.getTenantId());
		IPage<ReviewExpertVO> pages = reviewExpertService.selectReviewExpertPage(Condition.getPage(query), reviewExpert);
		return R.data(pages);
	}

	/**
	 * 新增 评标专家库
	 */
	@PostMapping("/save")
	@Operation(summary = "新增", description = "传入reviewExpert")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R save(@Valid @RequestBody ReviewExpert reviewExpert) {
		return R.status(reviewExpertService.save(reviewExpert));
	}

	/**
	 * 修改 评标专家库
	 */
	@PostMapping("/update")
	@Operation(summary = "修改", description = "传入reviewExpert")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R update(@Valid @RequestBody ReviewExpert reviewExpert) {
		return R.status(reviewExpertService.updateById(reviewExpert));
	}

	/**
	 * 新增或修改 评标专家库
	 */
	@PostMapping("/submit")
	@Operation(summary = "新增或修改", description = "传入reviewExpert")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R submit(@Valid @RequestBody ReviewExpertDTO reviewExpert) {
		return R.status(reviewExpertService.submit(reviewExpert));
	}


	/**
	 * 删除 评标专家库
	 */
	@PostMapping("/remove")
	@Operation(summary = "逻辑删除", description = "传入ids")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R remove(@Parameter(description = "主键集合", required = true) @RequestParam String ids) {
		return R.status(reviewExpertService.deleteLogic(Func.toLongList(ids)));
	}


	/**
	 * 导出
	 */
	@GetMapping("/export")
	@Operation(summary = "导出评标专家", description = "按当前查询条件导出评标专家数据")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public void export(
		@RequestParam Map<String, Object> params,
		Query query,
		@RequestParam(defaultValue = "page") String scope,
		HttpServletResponse response) {

		if (!"page".equals(scope) && !"all".equals(scope)) {
			throw new IllegalArgumentException("scope 仅支持 page 或 all");
		}

		// 防止前端传入其他租户编号
		params.put("tenantCode_equal", SecureUtil.getTenantId());

		// scope 只是导出控制参数，不能参与数据库查询
		params.remove("scope");

		List<ReviewExpertExportExcel> list =
			reviewExpertService.exportReviewExpert(params, query, scope);

		ExcelUtil.export(
			response,
			"评标专家数据",
			"评标专家",
			list,
			ReviewExpertExportExcel.class
		);
	}

	/**
	 * 下载导入模板
	 */
	@GetMapping("/import-template")
	@Operation(summary = "下载评标专家导入模板")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public void downloadImportTemplate(HttpServletResponse response) {
		ExcelUtil.export(
			response,
			"评标专家导入模板",
			"评标专家",
			Collections.emptyList(),
			ReviewExpertImportExcel.class
		);
	}

	/**
	 * 导入评标专家
	 */
	@PostMapping("/import")
	@Operation(summary = "导入评标专家")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<ReviewExpertImportResult> importReviewExpert(
		@RequestPart("file") MultipartFile file) {

		String filename = file.getOriginalFilename();
		if (!StringUtils.hasText(filename)) {
			return R.fail("请选择需要导入的Excel文件");
		}

		String lowerFilename = filename.toLowerCase(Locale.ROOT);
		if (!lowerFilename.endsWith(".xls") && !lowerFilename.endsWith(".xlsx")) {
			return R.fail("仅支持xls或xlsx格式的文件");
		}
		if (file.isEmpty()) {
			return R.fail("上传文件不能为空");
		}
		if (file.getSize() > 20 * 1024 * 1024L) {
			return R.fail("上传文件不能超过20MB");
		}

		List<ReviewExpertImportExcel> rows;
		try (InputStream input = new BufferedInputStream(file.getInputStream())) {
			rows = FastExcel
				.read(input)
				.head(ReviewExpertImportExcel.class)
				.sheet(0)
				.headRowNumber(1)
				.doReadSync();
		} catch (Exception exception) {
			log.error("导入评标专家Excel失败，文件名：{}", filename, exception);
			return R.fail("Excel解析失败，请检查文件是否使用正确模板");
		}

		return R.data(reviewExpertService.importReviewExpert(rows));
	}
}

