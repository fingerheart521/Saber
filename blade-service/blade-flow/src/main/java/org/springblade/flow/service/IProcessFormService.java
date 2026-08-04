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
import org.springblade.flow.dto.ProcessFormDTO;
import org.springblade.flow.dto.ProcessFormDesignDTO;
import org.springblade.flow.vo.ProcessDesignHistoryVO;
import org.springblade.flow.vo.ProcessFormVO;

import java.util.List;

/**
 * 流程表单服务接口
 *
 * @author Chill
 */
public interface IProcessFormService {

	IPage<ProcessFormVO> page(Integer current, Integer size, String key, String name, String category, Integer status);

	List<String> categories();

	List<ProcessFormVO> options();

	ProcessFormVO detail(String formId);

	ProcessFormVO detailByKey(String formKey);

	ProcessFormVO save(ProcessFormDTO processFormDTO);

	ProcessFormVO saveDesign(ProcessFormDesignDTO processFormDesignDTO);

	ProcessFormVO copy(ProcessDesignCopyDTO processDesignCopyDTO);

	List<ProcessDesignHistoryVO> history(String formId);

	ProcessDesignHistoryVO historyPreview(String formId, String historyId);

	ProcessFormVO setMainVersion(String formId, String historyId);

	boolean remove(String formId);

}
