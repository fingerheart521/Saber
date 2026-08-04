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
import org.springblade.flow.dto.ProcessDesignCopyDTO;
import org.springblade.flow.dto.ProcessModelDTO;
import org.springblade.flow.dto.ProcessModelDesignDTO;
import org.springblade.flow.vo.ProcessDesignHistoryVO;
import org.springblade.flow.vo.ProcessModelVO;

import java.util.List;

/**
 * 流程模型服务接口
 *
 * @author Chill
 */
public interface IProcessModelService {

	/**
	 * 分页查询当前租户的流程模型
	 *
	 * @param current  当前页
	 * @param size     每页数量
	 * @param key      模型key
	 * @param name     模型名称
	 * @param category 模型分类
	 * @return 流程模型分页
	 */
	IPage<ProcessModelVO> page(Integer current, Integer size, String key, String name, String category);

	/**
	 * 查询当前租户的模型分类
	 *
	 * @return 模型分类
	 */
	List<String> categories();

	/**
	 * 查询流程模型详情
	 *
	 * @param modelId 模型ID
	 * @return 流程模型详情
	 */
	ProcessModelVO detail(String modelId);

	/**
	 * 新增或修改流程模型基本信息
	 *
	 * @param processModelDTO 流程模型参数
	 * @return 流程模型
	 */
	ProcessModelVO save(ProcessModelDTO processModelDTO);

	/**
	 * 保存表单与流程设计
	 *
	 * @param processModelDesignDTO 流程模型设计参数
	 * @return 流程模型
	 */
	ProcessModelVO saveDesign(ProcessModelDesignDTO processModelDesignDTO);

	/**
	 * 复制流程模型及当前设计稿
	 *
	 * @param processDesignCopyDTO 复制参数
	 * @return 复制后的流程模型
	 */
	ProcessModelVO copy(ProcessDesignCopyDTO processDesignCopyDTO);

	/**
	 * 查询模型设计历史
	 *
	 * @param modelId 模型ID
	 * @return 设计历史
	 */
	List<ProcessDesignHistoryVO> history(String modelId);

	/**
	 * 预览模型设计历史
	 *
	 * @param modelId   模型ID
	 * @param historyId 历史快照ID
	 * @return 历史快照
	 */
	ProcessDesignHistoryVO historyPreview(String modelId, String historyId);

	/**
	 * 将历史设计恢复为新的主版本
	 *
	 * @param modelId   模型ID
	 * @param historyId 历史快照ID
	 * @return 新主版本模型
	 */
	ProcessModelVO setMainVersion(String modelId, String historyId);

	/**
	 * 部署流程模型
	 *
	 * @param modelId 模型ID
	 * @return 流程模型
	 */
	ProcessModelVO deploy(String modelId);

	/**
	 * 删除流程模型
	 *
	 * @param modelId 模型ID
	 * @return 是否成功
	 */
	boolean remove(String modelId);

}
