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
import org.springblade.flow.dto.ProcessModelDTO;
import org.springblade.flow.dto.ProcessModelDesignDTO;
import org.springblade.flow.service.IProcessModelService;
import org.springblade.flow.vo.ProcessDesignHistoryVO;
import org.springblade.flow.vo.ProcessModelVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程模型控制器
 *
 * @author Chill
 */
@RestController
@AllArgsConstructor
@RequestMapping("/process/model")
@ApiOrder
@Tag(name = "流程模型", description = "流程模型设计与部署")
public class ProcessModelController {

	private final IProcessModelService processModelService;

	@GetMapping("/list")
	@Operation(summary = "流程模型分页", description = "分页查询当前租户的流程模型")
	public R<IPage<ProcessModelVO>> list(
		@RequestParam(defaultValue = "1") Integer current,
		@RequestParam(defaultValue = "10") Integer size,
		@RequestParam(required = false) String key,
		@RequestParam(required = false) String name,
		@RequestParam(required = false) String category) {
		return R.data(processModelService.page(current, size, key, name, category));
	}

	@GetMapping("/categories")
	@Operation(summary = "流程模型分类", description = "查询当前租户已有的模型分类")
	public R<List<String>> categories() {
		return R.data(processModelService.categories());
	}

	@GetMapping("/detail")
	@Operation(summary = "流程模型详情", description = "查询模型元数据、表单配置与BPMN XML")
	public R<ProcessModelVO> detail(
		@Parameter(description = "模型ID", required = true)
		@RequestParam String modelId) {
		return R.data(processModelService.detail(modelId));
	}

	@PostMapping("/save")
	@Operation(summary = "保存流程模型", description = "新增或修改流程模型基本信息")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<ProcessModelVO> save(@Valid @RequestBody ProcessModelDTO processModelDTO) {
		return R.data(processModelService.save(processModelDTO));
	}

	@PostMapping("/save-design")
	@Operation(summary = "保存模型设计", description = "保存表单配置与BPMN XML")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<ProcessModelVO> saveDesign(@Valid @RequestBody ProcessModelDesignDTO processModelDesignDTO) {
		return R.data(processModelService.saveDesign(processModelDesignDTO));
	}

	@PostMapping("/copy")
	@Operation(summary = "复制流程模型", description = "复制模型基本信息、表单配置和BPMN设计，不复制部署记录")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<ProcessModelVO> copy(@Valid @RequestBody ProcessDesignCopyDTO processDesignCopyDTO) {
		return R.data(processModelService.copy(processDesignCopyDTO));
	}

	@GetMapping("/history")
	@Operation(summary = "模型设计历史", description = "查询模型每次保存设计生成的历史版本")
	public R<List<ProcessDesignHistoryVO>> history(
		@Parameter(description = "模型ID", required = true)
		@RequestParam String modelId) {
		return R.data(processModelService.history(modelId));
	}

	@GetMapping("/history-preview")
	@Operation(summary = "模型历史预览", description = "查询指定模型历史版本的BPMN设计")
	public R<ProcessDesignHistoryVO> historyPreview(
		@Parameter(description = "模型ID", required = true)
		@RequestParam String modelId,
		@Parameter(description = "历史快照ID", required = true)
		@RequestParam String historyId) {
		return R.data(processModelService.historyPreview(modelId, historyId));
	}

	@PostMapping("/set-main-version")
	@Operation(summary = "设置模型主版本", description = "将历史设计恢复为新的主版本，不自动部署且不影响运行实例")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<ProcessModelVO> setMainVersion(
		@Parameter(description = "模型ID", required = true)
		@RequestParam String modelId,
		@Parameter(description = "历史快照ID", required = true)
		@RequestParam String historyId) {
		return R.data(processModelService.setMainVersion(modelId, historyId));
	}

	@PostMapping("/deploy")
	@Operation(summary = "部署流程模型", description = "使用模型中的BPMN XML部署流程定义")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<ProcessModelVO> deploy(
		@Parameter(description = "模型ID", required = true)
		@RequestParam String modelId) {
		return R.data(processModelService.deploy(modelId));
	}

	@PostMapping("/remove")
	@Operation(summary = "删除流程模型", description = "删除模型及其编辑源，不删除已部署的流程定义")
	@PreAuth(RoleConstant.HAS_ROLE_ADMIN)
	public R<Void> remove(
		@Parameter(description = "模型ID", required = true)
		@RequestParam String modelId) {
		return R.status(processModelService.remove(modelId));
	}

}
