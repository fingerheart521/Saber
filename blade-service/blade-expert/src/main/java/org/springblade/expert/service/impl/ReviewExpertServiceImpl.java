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
package org.springblade.expert.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.mp.support.Condition;
import org.springblade.core.mp.support.Query;
import org.springblade.core.secure.utils.SecureUtil;
import org.springblade.core.tool.utils.BeanUtil;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.expert.excel.ReviewExpertExportExcel;
import org.springblade.expert.excel.ReviewExpertImportExcel;
import org.springblade.expert.mapper.ReviewExpertFileMapper;
import org.springblade.expert.mapper.ReviewExpertMapper;
import org.springblade.expert.pojo.dto.ReviewExpertDTO;
import org.springblade.expert.pojo.dto.ReviewExpertFileDTO;
import org.springblade.expert.pojo.dto.ReviewExpertImportResult;
import org.springblade.expert.pojo.entity.ReviewExpert;
import org.springblade.expert.pojo.entity.ReviewExpertFile;
import org.springblade.expert.pojo.vo.ReviewExpertFileVO;
import org.springblade.expert.pojo.vo.ReviewExpertVO;
import org.springblade.expert.service.IReviewExpertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评标专家库 服务实现类
 *
 * @author Chill
 * @since 2026-07-23
 */
@RequiredArgsConstructor
@Service
public class ReviewExpertServiceImpl extends ServiceImpl<ReviewExpertMapper, ReviewExpert> implements IReviewExpertService {

	private static final Set<String> EXPERT_SOURCE_TYPES = Set.of("1", "2");
	private static final Set<String> SEX_TYPES = Set.of("1", "2");
	private static final Set<String> EXPERT_FILE_TYPES = Set.of("pdf", "jpg", "jpeg", "png");
	private static final int MAX_EXPERT_FILES = 5;
	private static final long MAX_EXPERT_FILE_SIZE = 10 * 1024 * 1024L;

	private final ReviewExpertFileMapper reviewExpertFileMapper;
	@Override
	public IPage<ReviewExpertVO> selectReviewExpertPage(IPage<ReviewExpertVO> page, ReviewExpertVO reviewExpert) {
		return page.setRecords(baseMapper.selectReviewExpertPage(page, reviewExpert));
	}
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean submit(ReviewExpertDTO entity) {
		String tenantCode = SecureUtil.getTenantId();
		String account = SecureUtil.getUserAccount();
		String userName = SecureUtil.getUserName();
		Date now = DateUtil.now();

		entity.setTenantCode(tenantCode);
		entity.setUpdateBy(account);
		entity.setUpdateName(userName);
		entity.setUpdateTime(now);
		entity.setDelFlag("0");

		boolean saved;
		if (entity.getId() == null) {
			entity.setCreateBy(account);
			entity.setCreateName(userName);
			entity.setCreateTime(now);
			saved = save(entity);
		} else {
			saved = update(
				entity,
				Wrappers.<ReviewExpert>lambdaUpdate()
					.eq(ReviewExpert::getId, entity.getId())
					.eq(ReviewExpert::getTenantCode, tenantCode)
					.eq(ReviewExpert::getDelFlag, "0")
			);
		}

		if (!saved) {
			return false;
		}

		List<ReviewExpertFileDTO> requestFiles = entity.getExpertFileList() == null
			? entity.getExpertFileNameList()
			: entity.getExpertFileList();
		saveExpertFiles(entity.getId(), requestFiles, tenantCode, account, userName, now);
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteLogic(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return false;
		}

		String tenantCode = SecureUtil.getTenantId();
		List<Long> expertIds = list(
			Wrappers.<ReviewExpert>lambdaQuery()
				.select(ReviewExpert::getId)
				.in(ReviewExpert::getId, ids)
				.eq(ReviewExpert::getTenantCode, tenantCode)
				.eq(ReviewExpert::getDelFlag, "0")
		).stream().map(ReviewExpert::getId).toList();
		if (expertIds.isEmpty()) {
			return false;
		}

		boolean removed = remove(
			Wrappers.<ReviewExpert>lambdaQuery()
				.in(ReviewExpert::getId, expertIds)
				.eq(ReviewExpert::getTenantCode, tenantCode)
				.eq(ReviewExpert::getDelFlag, "0")
		);
		if (!removed) {
			return false;
		}

		reviewExpertFileMapper.delete(
			Wrappers.<ReviewExpertFile>lambdaQuery()
				.in(ReviewExpertFile::getExpertId, expertIds)
				.eq(ReviewExpertFile::getTenantCode, tenantCode)
				.eq(ReviewExpertFile::getDelFlag, "0")
		);
		return true;
	}

	@Override
	public List<ReviewExpertExportExcel> exportReviewExpert(
		Map<String, Object> params,
		Query query,
		String scope) {

		QueryWrapper<ReviewExpert> wrapper =
			Condition.getQueryWrapper(params, ReviewExpert.class);

		List<ReviewExpert> records;

		if ("all".equals(scope)) {
			records = list(wrapper);
		} else {
			records = page(
				Condition.getPage(query),
				wrapper
			).getRecords();
		}

		return records.stream()
			.map(this::toExportExcel)
			.collect(Collectors.toList());
	}

	private ReviewExpertExportExcel toExportExcel(ReviewExpert source) {
		ReviewExpertExportExcel target = new ReviewExpertExportExcel();

		target.setExpertCode(source.getExpertCode());
		target.setExpertName(source.getExpertName());
		target.setProfessionalCodes(
			source.getProfessionalCodes() == null
				? ""
				: String.join(",", source.getProfessionalCodes())
		);
		target.setDeptName(source.getDeptName());
		target.setDepartmentName(source.getDepartmentName());
		target.setMajorYears(source.getMajorYears());
		target.setApprovalStatus(
			getApprovalStatusName(source.getApprovalStatus())
		);
		target.setCreateName(source.getCreateName());
		target.setCreateTime(source.getCreateTime());
		target.setUpdateName(source.getUpdateName());
		target.setUpdateTime(source.getUpdateTime());

		return target;
	}

	private String getApprovalStatusName(String status) {
		if (status == null) {
			return "";
		}

		return switch (status) {
			case "0" -> "未审批";
			case "1" -> "准入审批中";
			case "2" -> "准入驳回";
			case "3" -> "已准入";
			case "4" -> "清退审批中";
			case "5" -> "清退驳回";
			case "6" -> "已清退";
			default -> status;
		};
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public ReviewExpertImportResult importReviewExpert(List<ReviewExpertImportExcel> rows) {
		ReviewExpertImportResult result = new ReviewExpertImportResult();
		int total = rows == null ? 0 : rows.size();
		result.setTotalCount(total);

		if (total == 0) {
			result.getErrors().add(new ReviewExpertImportResult.RowError(2, "没有可导入的数据"));
			result.setFailureCount(1);
			return result;
		}
		if (total > 5000) {
			result.getErrors().add(new ReviewExpertImportResult.RowError(2, "一次最多导入5000条数据"));
			result.setFailureCount(total);
			return result;
		}

		Map<String, Integer> firstRowByCode = new HashMap<>();
		Set<String> expertCodes = new HashSet<>();

		for (int index = 0; index < rows.size(); index++) {
			ReviewExpertImportExcel row = rows.get(index);
			int rowNumber = index + 2; // 第1行是Excel表头
			List<String> messages = new ArrayList<>();

			row.setExpertCode(trim(row.getExpertCode()));
			row.setExpertName(trim(row.getExpertName()));
			row.setExpertSourceType(trim(row.getExpertSourceType()));
			row.setSex(trim(row.getSex()));
			row.setPhone(trim(row.getPhone()));
			row.setMajorYears(trim(row.getMajorYears()));
			row.setProfessionalCodes(trim(row.getProfessionalCodes()));
			row.setDeptCode(trim(row.getDeptCode()));
			row.setDeptName(trim(row.getDeptName()));
			row.setDepartmentCode(trim(row.getDepartmentCode()));
			row.setDepartmentName(trim(row.getDepartmentName()));
			row.setRemark(trim(row.getRemark()));

			if (!StringUtils.hasText(row.getExpertCode())) {
				messages.add("专家账号不能为空");
			} else {
				Integer firstRow = firstRowByCode.putIfAbsent(row.getExpertCode(), rowNumber);
				if (firstRow != null) {
					messages.add("专家账号与第" + firstRow + "行重复");
				}
				expertCodes.add(row.getExpertCode());
			}
			if (!StringUtils.hasText(row.getExpertName())) {
				messages.add("专家姓名不能为空");
			}
			if (!EXPERT_SOURCE_TYPES.contains(row.getExpertSourceType())) {
				messages.add("专家来源只能填写1（内部专家）或2（外部专家）");
			}
			if (StringUtils.hasText(row.getSex()) && !SEX_TYPES.contains(row.getSex())) {
				messages.add("性别只能填写1（男）或2（女）");
			}
			if (StringUtils.hasText(row.getPhone()) && !row.getPhone().matches("^1\\d{10}$")) {
				messages.add("手机号格式不正确");
			}
			if (splitCodes(row.getProfessionalCodes()).isEmpty()) {
				messages.add("专业编码不能为空");
			}
			if (!StringUtils.hasText(row.getDeptCode())) {
				messages.add("部门编码不能为空");
			}

			if (!messages.isEmpty()) {
				result.getErrors().add(
					new ReviewExpertImportResult.RowError(rowNumber, String.join("；", messages))
				);
			}
		}

		// 仅查询当前租户、未删除数据，判断数据库重复账号。
		if (!expertCodes.isEmpty()) {
			String tenantCode = SecureUtil.getTenantId();
			List<String> existingCodes = list(
				Wrappers.<ReviewExpert>lambdaQuery()
					.select(ReviewExpert::getExpertCode)
					.eq(ReviewExpert::getTenantCode, tenantCode)
					.eq(ReviewExpert::getDelFlag, "0")
					.in(ReviewExpert::getExpertCode, expertCodes)
			).stream().map(ReviewExpert::getExpertCode).collect(Collectors.toList());

			Set<String> existingCodeSet = new HashSet<>(existingCodes);
			for (int index = 0; index < rows.size(); index++) {
				String code = rows.get(index).getExpertCode();
				if (StringUtils.hasText(code) && existingCodeSet.contains(code)) {
					result.getErrors().add(
						new ReviewExpertImportResult.RowError(index + 2, "专家账号已存在于当前租户")
					);
				}
			}
		}

		// 整批校验：只要有一行错误，本次一条都不保存。
		if (!result.getErrors().isEmpty()) {
			int failureCount = (int) result.getErrors().stream()
				.map(ReviewExpertImportResult.RowError::getRowNumber)
				.distinct()
				.count();
			result.setFailureCount(failureCount);
			return result;
		}

		String tenantCode = SecureUtil.getTenantId();
		String account = SecureUtil.getUserAccount();
		String userName = SecureUtil.getUserName();
		Date now = DateUtil.now();

		List<ReviewExpert> entities = rows.stream().map(row -> {
			ReviewExpert entity = new ReviewExpert();
			entity.setExpertCode(row.getExpertCode());
			entity.setExpertName(row.getExpertName());
			entity.setExpertSourceType(trim(row.getExpertSourceType()));
			entity.setSex(trim(row.getSex()));
			entity.setPhone(row.getPhone());
			entity.setMajorYears(trim(row.getMajorYears()));
			entity.setProfessionalCodes(splitCodes(row.getProfessionalCodes()));
			entity.setDeptCode(row.getDeptCode());
			entity.setDeptName(row.getDeptName());
			entity.setDepartmentCode(row.getDepartmentCode());
			entity.setDepartmentName(row.getDepartmentName());
			entity.setRemark(trim(row.getRemark()));

			entity.setTenantCode(tenantCode);
			entity.setApprovalStatus("0");
			entity.setEnableStatus("Y");
			entity.setDelFlag("0");
			entity.setCreateBy(account);
			entity.setCreateName(userName);
			entity.setCreateTime(now);
			entity.setUpdateBy(account);
			entity.setUpdateName(userName);
			entity.setUpdateTime(now);
			return entity;
		}).collect(Collectors.toList());

		saveBatch(entities, 500);
		result.setSuccessCount(entities.size());
		return result;
	}

	private String trim(String value) {
		return value == null ? null : value.trim();
	}

	private List<String> splitCodes(String value) {
		if (!StringUtils.hasText(value)) {
			return new ArrayList<>();
		}
		return new ArrayList<>(new LinkedHashSet<>(
			Arrays.stream(value.split("[,，;；]"))
				.map(String::trim)
				.filter(StringUtils::hasText)
				.collect(Collectors.toList())
		));
	}

	@Override
	public ReviewExpertVO detail(Long id) {
		if (id == null) {
			return null;
		}

		String tenantCode = SecureUtil.getTenantId();
		ReviewExpert expert = getOne(
			Wrappers.<ReviewExpert>lambdaQuery()
				.eq(ReviewExpert::getId, id)
				.eq(ReviewExpert::getTenantCode, tenantCode)
				.eq(ReviewExpert::getDelFlag, "0")
		);

		if (expert == null) {
			return null;
		}

		ReviewExpertVO result = BeanUtil.copyProperties(expert, ReviewExpertVO.class);
		List<ReviewExpertFileVO> fileList = reviewExpertFileMapper.selectList(
			Wrappers.<ReviewExpertFile>lambdaQuery()
				.eq(ReviewExpertFile::getTenantCode, tenantCode)
				.eq(ReviewExpertFile::getExpertId, id)
				.eq(ReviewExpertFile::getDelFlag, "0")
				.orderByAsc(ReviewExpertFile::getCreateTime)
				.orderByAsc(ReviewExpertFile::getId)
		).stream().map(this::toFileVO).toList();

		result.setExpertFileList(fileList);
		result.setExpertFileNameList(new ArrayList<>(fileList));
		return result;
	}

	private ReviewExpertFileVO toFileVO(ReviewExpertFile source) {
		ReviewExpertFileVO target = BeanUtil.copyProperties(source, ReviewExpertFileVO.class);
		target.setLink(source.getFileUrl());
		target.setUrl(source.getFileUrl());
		target.setOriginalName(source.getFileName());
		return target;
	}

	private void saveExpertFiles(Long expertId, List<ReviewExpertFileDTO> requestFiles,
		String tenantCode, String account, String userName, Date now) {
		List<ReviewExpertFileDTO> files = requestFiles == null ? List.of() : requestFiles;
		if (files.size() > MAX_EXPERT_FILES) {
			throw new ServiceException("证明附件最多上传5个");
		}

		reviewExpertFileMapper.delete(
			Wrappers.<ReviewExpertFile>lambdaQuery()
				.eq(ReviewExpertFile::getTenantCode, tenantCode)
				.eq(ReviewExpertFile::getExpertId, expertId)
				.eq(ReviewExpertFile::getDelFlag, "0")
		);

		Set<String> fileUrls = new HashSet<>();
		for (ReviewExpertFileDTO requestFile : files) {
			if (requestFile == null) {
				throw new ServiceException("证明附件信息不能为空");
			}
			String fileUrl = firstText(requestFile.getFileUrl(), requestFile.getLink(), requestFile.getUrl());
			String fileName = firstText(requestFile.getFileName(), requestFile.getOriginalName());
			if (!StringUtils.hasText(fileUrl) || !StringUtils.hasText(fileName)) {
				throw new ServiceException("证明附件地址和文件名不能为空");
			}
			if (fileName.length() > 100) {
				throw new ServiceException("证明附件名称不能超过100个字符");
			}
			if (fileUrl.length() > 500) {
				throw new ServiceException("证明附件地址不能超过500个字符");
			}
			if (!fileUrls.add(fileUrl)) {
				throw new ServiceException("证明附件不能重复");
			}

			String fileType = getExtension(fileName);
			if (!EXPERT_FILE_TYPES.contains(fileType)) {
				throw new ServiceException("证明附件仅支持PDF、JPG、JPEG、PNG格式");
			}
			Long fileSize = requestFile.getFileSize();
			if (fileSize != null && (fileSize < 0 || fileSize > MAX_EXPERT_FILE_SIZE)) {
				throw new ServiceException("证明附件不能超过10MB");
			}

			ReviewExpertFile target = new ReviewExpertFile();
			target.setExpertId(expertId);
			target.setResourceId(requestFile.getResourceId());
			target.setFileName(fileName);
			target.setFileUrl(fileUrl);
			target.setFileType(fileType);
			target.setFileSize(fileSize);
			target.setTenantCode(tenantCode);
			target.setCreateBy(account);
			target.setCreateName(userName);
			target.setCreateTime(now);
			target.setUpdateBy(account);
			target.setUpdateName(userName);
			target.setUpdateTime(now);
			target.setDelFlag("0");
			reviewExpertFileMapper.insert(target);
		}
	}

	private String firstText(String... values) {
		return Arrays.stream(values)
			.filter(StringUtils::hasText)
			.map(String::trim)
			.findFirst()
			.orElse(null);
	}

	private String getExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		return dotIndex < 0 ? "" : fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
	}

}
