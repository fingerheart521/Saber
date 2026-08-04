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
import org.springblade.flow.dto.StartProcessDTO;
import org.springblade.flow.vo.ProcessHistoryVO;
import org.springblade.flow.vo.ProcessInstanceVO;

import java.util.List;

/**
 * 流程实例服务类
 *
 * @author Chill
 */
public interface IProcessInstanceService {

	ProcessInstanceVO start(StartProcessDTO startProcessDTO);

	IPage<ProcessInstanceVO> mine(Integer current, Integer size);

	ProcessInstanceVO detail(String processInstanceId);

	List<ProcessHistoryVO> history(String processInstanceId);

	boolean cancel(String processInstanceId, String reason);

	boolean remove(String processInstanceId);

}
