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
package org.springblade.flow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springblade.flow.dto.CompleteTaskDTO;
import org.springblade.flow.dto.TaskActionDTO;
import org.springblade.flow.vo.ProcessRuntimeFormVO;
import org.springblade.flow.vo.ProcessTaskVO;

/**
 * 流程任务服务类
 *
 * @author Chill
 */
public interface IProcessTaskService {

	IPage<ProcessTaskVO> todo(Integer current, Integer size);

	IPage<ProcessTaskVO> done(Integer current, Integer size);

	ProcessRuntimeFormVO form(String taskId);

	boolean claim(String taskId);

	boolean unclaim(String taskId);

	boolean complete(CompleteTaskDTO completeTaskDTO);

	boolean action(TaskActionDTO taskActionDTO);

}
