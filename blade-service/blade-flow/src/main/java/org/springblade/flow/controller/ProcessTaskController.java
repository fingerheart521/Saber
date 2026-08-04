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
import org.springblade.flow.dto.CompleteTaskDTO;
import org.springblade.flow.dto.TaskActionDTO;
import org.springblade.flow.service.IProcessTaskService;
import org.springblade.flow.vo.ProcessRuntimeFormVO;
import org.springblade.flow.vo.ProcessTaskVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程任务控制器
 *
 * @author Chill
 */
@RestController
@AllArgsConstructor
@RequestMapping("/process/task")
@ApiOrder
@Tag(name = "流程任务", description = "待办、已办、签收与审批")
public class ProcessTaskController {

	private final IProcessTaskService processTaskService;

	@GetMapping("/todo")
	@Operation(summary = "查询当前用户待办")
	public R<IPage<ProcessTaskVO>> todo(
		@RequestParam(defaultValue = "1") Integer current,
		@RequestParam(defaultValue = "10") Integer size) {
		return R.data(processTaskService.todo(current, size));
	}

	@GetMapping("/done")
	@Operation(summary = "查询当前用户已办")
	public R<IPage<ProcessTaskVO>> done(
		@RequestParam(defaultValue = "1") Integer current,
		@RequestParam(defaultValue = "10") Integer size) {
		return R.data(processTaskService.done(current, size));
	}

	@GetMapping("/form")
	@Operation(summary = "查询任务运行时表单")
	public R<ProcessRuntimeFormVO> form(
		@Parameter(description = "任务ID", required = true)
		@RequestParam String taskId) {
		return R.data(processTaskService.form(taskId));
	}

	@PostMapping("/claim")
	@Operation(summary = "签收候选任务")
	public R<Void> claim(
		@Parameter(description = "任务ID", required = true)
		@RequestParam String taskId) {
		return R.status(processTaskService.claim(taskId));
	}

	@PostMapping("/unclaim")
	@Operation(summary = "取消签收任务")
	public R<Void> unclaim(
		@Parameter(description = "任务ID", required = true)
		@RequestParam String taskId) {
		return R.status(processTaskService.unclaim(taskId));
	}

	@PostMapping("/complete")
	@Operation(summary = "完成流程任务")
	public R<Void> complete(@Valid @RequestBody CompleteTaskDTO completeTaskDTO) {
		return R.status(processTaskService.complete(completeTaskDTO));
	}

	@PostMapping("/action")
	@Operation(summary = "执行流程任务动作", description = "按节点按钮配置执行通过、退回、转办、委托、终止、加减签、指定回退、暂存或撤销")
	public R<Void> action(@Valid @RequestBody TaskActionDTO taskActionDTO) {
		return R.status(processTaskService.action(taskActionDTO));
	}

}
