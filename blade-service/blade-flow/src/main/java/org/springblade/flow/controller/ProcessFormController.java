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
package org.springblade.flow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springblade.core.secure.annotation.PreAuth;
import org.springblade.core.swagger.annotation.ApiOrder;
import org.springblade.core.tool.api.R;
import org.springblade.core.tool.constant.RoleConstant;
import org.springblade.flow.dto.ProcessDesignCopyDTO;
import org.springblade.flow.dto.ProcessFormDTO;
import org.springblade.flow.dto.ProcessFormDesignDTO;
import org.springblade.flow.service.IProcessFormService;
import org.springblade.flow.vo.ProcessDesignHistoryVO;
import org.springblade.flow.vo.ProcessFormVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程表单控制器
 *
 * @author Chill
 */
@RestController
@AllArgsConstructor
@RequestMapping("/process/form")
@ApiOrder
@Tag(name = "流程表单", description = "流程表单设计与维护")
public class ProcessFormController {

	private final IProcessFormService processFormService;

	@GetMapping("/list")
	@Operation(summary = "流程表单分页", description = "分页查询当前租户的流程表单")
	public R<IPage<ProcessFormVO>> list(
		@RequestParam(defaultValue = "1") Integer current,
		@RequestParam(defaultValue = "10") Integer size,
		@RequestParam(required = false) String key,
		@RequestParam(required = false) String name,
		@RequestParam(required = false) String category,
		@RequestParam(required = false) Integer status) {
		return R.data(processFormService.page(current, size, key, name, category, status));
	}

	@GetMapping("/categories")
	@Operation(summary = "流程表单分类", description = "查询当前租户已有的表单分类")
	public R<List<String>> categories() {
		return R.data(processFormService.categories());
	}

	@GetMapping("/options")
	@Operation(summary = "可用流程表单", description = "查询当前租户已启用的流程表单")
	public R<List<ProcessFormVO>> options() {
		return R.data(processFormService.options());
	}

	@GetMapping("/detail")
	@Operation(summary = "流程表单详情", description = "查询表单元数据与设计JSON")
	public R<ProcessFormVO> detail(
		@Parameter(description = "表单ID", required = true)
		@RequestParam String formId) {
		return R.data(processFormService.detail(formId));
	}

	@PostMapping("/save")
	@Operation(summary = "保存流程表单", description = "新增或修改流程表单基本信息")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<ProcessFormVO> save(@Valid @RequestBody ProcessFormDTO processFormDTO) {
		return R.data(processFormService.save(processFormDTO));
	}

	@PostMapping("/save-design")
	@Operation(summary = "保存表单设计", description = "保存Avue表单设计JSON")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<ProcessFormVO> saveDesign(@Valid @RequestBody ProcessFormDesignDTO processFormDesignDTO) {
		return R.data(processFormService.saveDesign(processFormDesignDTO));
	}

	@PostMapping("/copy")
	@Operation(summary = "复制流程表单", description = "复制表单基本信息与当前Avue设计，不复制历史版本")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<ProcessFormVO> copy(@Valid @RequestBody ProcessDesignCopyDTO processDesignCopyDTO) {
		return R.data(processFormService.copy(processDesignCopyDTO));
	}

	@GetMapping("/history")
	@Operation(summary = "表单设计历史", description = "查询表单每次保存设计生成的历史版本")
	public R<List<ProcessDesignHistoryVO>> history(
		@Parameter(description = "表单ID", required = true)
		@RequestParam String formId) {
		return R.data(processFormService.history(formId));
	}

	@GetMapping("/history-preview")
	@Operation(summary = "表单历史预览", description = "查询指定表单历史版本的Avue设计")
	public R<ProcessDesignHistoryVO> historyPreview(
		@Parameter(description = "表单ID", required = true)
		@RequestParam String formId,
		@Parameter(description = "历史快照ID", required = true)
		@RequestParam String historyId) {
		return R.data(processFormService.historyPreview(formId, historyId));
	}

	@PostMapping("/set-main-version")
	@Operation(summary = "设置表单主版本", description = "将历史表单设计恢复为新的主版本")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<ProcessFormVO> setMainVersion(
		@Parameter(description = "表单ID", required = true)
		@RequestParam String formId,
		@Parameter(description = "历史快照ID", required = true)
		@RequestParam String historyId) {
		return R.data(processFormService.setMainVersion(formId, historyId));
	}

	@PostMapping("/remove")
	@Operation(summary = "删除流程表单", description = "删除流程表单及其设计内容")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<Void> remove(
		@Parameter(description = "表单ID", required = true)
		@RequestParam String formId) {
		return R.status(processFormService.remove(formId));
	}

}
