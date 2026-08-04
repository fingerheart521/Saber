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
import org.springblade.core.swagger.annotation.ApiOrder;
import org.springblade.core.tool.api.R;
import org.springblade.flow.dto.StartProcessDTO;
import org.springblade.flow.service.IProcessInstanceService;
import org.springblade.flow.vo.ProcessHistoryVO;
import org.springblade.flow.vo.ProcessInstanceVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 流程实例控制器
 *
 * @author Chill
 */
@RestController
@AllArgsConstructor
@RequestMapping("/process/instance")
@ApiOrder
@Tag(name = "流程实例", description = "流程发起、查询、历史与撤销")
public class ProcessInstanceController {

	private final IProcessInstanceService processInstanceService;

	@PostMapping("/start")
	@Operation(summary = "发起流程")
	public R<ProcessInstanceVO> start(@Valid @RequestBody StartProcessDTO startProcessDTO) {
		return R.data(processInstanceService.start(startProcessDTO));
	}

	@GetMapping("/mine")
	@Operation(summary = "查询我发起的流程")
	public R<IPage<ProcessInstanceVO>> mine(
		@RequestParam(defaultValue = "1") Integer current,
		@RequestParam(defaultValue = "10") Integer size) {
		return R.data(processInstanceService.mine(current, size));
	}

	@GetMapping("/detail")
	@Operation(summary = "查询流程实例详情")
	public R<ProcessInstanceVO> detail(
		@Parameter(description = "流程实例ID", required = true)
		@RequestParam String processInstanceId) {
		return R.data(processInstanceService.detail(processInstanceId));
	}

	@GetMapping("/history")
	@Operation(summary = "查询流程历史")
	public R<List<ProcessHistoryVO>> history(
		@Parameter(description = "流程实例ID", required = true)
		@RequestParam String processInstanceId) {
		return R.data(processInstanceService.history(processInstanceId));
	}

	@PostMapping("/cancel")
	@Operation(summary = "撤销运行中的流程")
	public R<Void> cancel(
		@Parameter(description = "流程实例ID", required = true)
		@RequestParam String processInstanceId,
		@Parameter(description = "撤销原因")
		@RequestParam(required = false) String reason) {
		return R.status(processInstanceService.cancel(processInstanceId, reason));
	}

	@PostMapping("/remove")
	@Operation(summary = "删除流程历史记录")
	public R<Void> remove(
		@Parameter(description = "流程实例ID", required = true)
		@RequestParam String processInstanceId) {
		return R.status(processInstanceService.remove(processInstanceId));
	}

}
