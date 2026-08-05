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
package org.springblade.procurement.reviewexpert.service;

import com.baomidou.mybatisplus.spring.service.IService;
import org.springblade.procurement.reviewexpert.excel.ReviewExpertImportExcel;
import org.springblade.procurement.reviewexpert.pojo.dto.ReviewExpertDTO;
import org.springblade.procurement.reviewexpert.pojo.dto.ReviewExpertImportResult;
import org.springblade.procurement.reviewexpert.pojo.entity.ReviewExpert;
import org.springblade.procurement.reviewexpert.pojo.vo.ReviewExpertVO;
//import org.springblade.core.mp.base.BaseService;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

import org.springblade.core.mp.support.Query;
import org.springblade.procurement.reviewexpert.excel.ReviewExpertExportExcel;

/**
 * 评标专家库 服务类
 *
 * @author Chill
 * @since 2026-07-23
 */
public interface IReviewExpertService extends IService<ReviewExpert> {

	/**
	 * 自定义分页
	 *
	 * @param page 分页参数
	 * @param reviewExpert 评标专家库查询条件
	 * @return 评标专家库分页数据
	 */
	IPage<ReviewExpertVO> selectReviewExpertPage(IPage<ReviewExpertVO> page, ReviewExpertVO reviewExpert);

	ReviewExpertVO detail(Long id);

	boolean submit(ReviewExpertDTO reviewExpert);

	boolean deleteLogic(List<Long> ids);
	/**
	 * 导出
	 */
	List<ReviewExpertExportExcel> exportReviewExpert(
		Map<String, Object> params,
		Query query,
		String scope
	);

	ReviewExpertImportResult importReviewExpert(
		List<ReviewExpertImportExcel> rows
	);

}

