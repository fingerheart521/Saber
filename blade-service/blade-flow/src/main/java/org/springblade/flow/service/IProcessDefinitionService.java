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
import org.springblade.flow.vo.ProcessDefinitionVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 流程定义服务接口
 *
 * @author Chill
 */
public interface IProcessDefinitionService {

	/**
	 * 分页查询当前租户的流程定义
	 *
	 * @param current 当前页
	 * @param size    每页数量
	 * @param key     流程标识
	 * @param name    流程名称
	 * @return 流程定义分页
	 */
	IPage<ProcessDefinitionVO> page(Integer current, Integer size, String key, String name);

	/**
	 * 部署BPMN流程定义
	 *
	 * @param file     BPMN文件
	 * @param category 流程分类
	 * @return 已部署的流程定义
	 */
	ProcessDefinitionVO deploy(MultipartFile file, String category);

	/**
	 * 挂起流程定义及其流程实例
	 *
	 * @param processDefinitionId 流程定义ID
	 * @return 是否成功
	 */
	boolean suspend(String processDefinitionId);

	/**
	 * 激活流程定义及其流程实例
	 *
	 * @param processDefinitionId 流程定义ID
	 * @return 是否成功
	 */
	boolean activate(String processDefinitionId);

	/**
	 * 下载流程定义的BPMN资源
	 *
	 * @param processDefinitionId 流程定义ID
	 * @return BPMN资源
	 */
	Resource download(String processDefinitionId);

	/**
	 * 删除无关联流程实例的部署版本
	 *
	 * @param processDefinitionId 流程定义ID
	 * @return 是否成功
	 */
	boolean remove(String processDefinitionId);

}
