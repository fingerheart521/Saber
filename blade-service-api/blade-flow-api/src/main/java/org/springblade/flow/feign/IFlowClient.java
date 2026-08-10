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
package org.springblade.flow.feign;

import jakarta.validation.Valid;
import org.springblade.common.constant.CommonConstant;
import org.springblade.core.tool.api.R;
import org.springblade.flow.dto.StartProcessDTO;
import org.springblade.flow.dto.CompleteTaskDTO;
import org.springblade.flow.vo.ProcessInstanceVO;
import org.springblade.flow.vo.ProcessTaskVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 流程服务Feign接口
 *
 * @author Chill
 */
@FeignClient(
	value = CommonConstant.APPLICATION_FLOW_NAME,
	fallback = IFlowClientFallback.class
)
public interface IFlowClient {

	String API_PREFIX = "/feign/client/flow";

	@PostMapping(API_PREFIX + "/start")
	R<ProcessInstanceVO> start(@Valid @RequestBody StartProcessDTO startProcessDTO);

	@GetMapping(API_PREFIX + "/instance")
	R<ProcessInstanceVO> instance(@RequestParam("processInstanceId") String processInstanceId);

	@PostMapping(API_PREFIX + "/cancel")
	R<Void> cancel(@RequestParam("processInstanceId") String processInstanceId,
		@RequestParam(value = "reason", required = false) String reason);

	@GetMapping(API_PREFIX + "/current-task")
	R<ProcessTaskVO> currentTask(@RequestParam("processInstanceId") String processInstanceId);

	@PostMapping(API_PREFIX + "/complete")
	R<Void> complete(@Valid @RequestBody CompleteTaskDTO completeTaskDTO);

}
