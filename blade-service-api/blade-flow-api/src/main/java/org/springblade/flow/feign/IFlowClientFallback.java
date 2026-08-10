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

import org.springblade.core.tool.api.R;
import org.springblade.flow.dto.StartProcessDTO;
import org.springblade.flow.dto.CompleteTaskDTO;
import org.springblade.flow.vo.ProcessInstanceVO;
import org.springblade.flow.vo.ProcessTaskVO;
import org.springframework.stereotype.Component;

/**
 * 流程服务Feign失败配置
 *
 * @author Chill
 */
@Component
public class IFlowClientFallback implements IFlowClient {

	@Override
	public R<ProcessInstanceVO> start(StartProcessDTO startProcessDTO) {
		return R.fail("流程服务暂不可用，发起流程失败");
	}

	@Override
	public R<ProcessInstanceVO> instance(String processInstanceId) {
		return R.fail("流程服务暂不可用，查询流程失败");
	}

	@Override
	public R<Void> cancel(String processInstanceId, String reason) {
		return R.fail("流程服务暂不可用，撤销流程失败");
	}

	@Override
	public R<ProcessTaskVO> currentTask(String processInstanceId) {
		return R.fail("流程服务暂不可用，查询待办失败");
	}

	@Override
	public R<Void> complete(CompleteTaskDTO completeTaskDTO) {
		return R.fail("流程服务暂不可用，执行审批失败");
	}

}
