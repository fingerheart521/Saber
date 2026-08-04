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
package org.springblade.flow.feign;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import org.springblade.core.tool.api.R;
import org.springblade.flow.dto.StartProcessDTO;
import org.springblade.flow.service.IProcessInstanceService;
import org.springblade.flow.vo.ProcessInstanceVO;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程服务Feign实现类
 *
 * @author Chill
 */
@Hidden
@RestController
@AllArgsConstructor
public class FlowClient implements IFlowClient {

	private final IProcessInstanceService processInstanceService;

	@Override
	public R<ProcessInstanceVO> start(StartProcessDTO startProcessDTO) {
		return R.data(processInstanceService.start(startProcessDTO));
	}

	@Override
	public R<ProcessInstanceVO> instance(String processInstanceId) {
		return R.data(processInstanceService.detail(processInstanceId));
	}

	@Override
	public R<Void> cancel(String processInstanceId, String reason) {
		return R.status(processInstanceService.cancel(processInstanceId, reason));
	}

}
