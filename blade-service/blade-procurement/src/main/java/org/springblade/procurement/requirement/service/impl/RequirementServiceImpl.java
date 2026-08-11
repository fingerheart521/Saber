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
package org.springblade.procurement.requirement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springblade.core.log.exception.ServiceException;
import org.springblade.core.secure.utils.SecureUtil;
import org.springblade.core.tool.utils.DateUtil;
import org.springblade.procurement.requirement.mapper.BiddingPartsDetailMapper;
import org.springblade.procurement.requirement.mapper.BiddingScrapDetailMapper;
import org.springblade.procurement.requirement.mapper.BiddingTrialDetailMapper;
import org.springblade.procurement.requirement.mapper.PurchaseRequirementDetailMapper;
import org.springblade.procurement.requirement.mapper.RequirementFileMapper;
import org.springblade.procurement.requirement.mapper.RequirementMapper;
import org.springblade.procurement.requirement.excel.RequirementExportExcel;
import org.springblade.procurement.requirement.pojo.dto.RequirementDTO;
import org.springblade.procurement.requirement.pojo.dto.RequirementPackageDTO;
import org.springblade.procurement.requirement.pojo.entity.BiddingPartsDetail;
import org.springblade.procurement.requirement.pojo.entity.BiddingScrapDetail;
import org.springblade.procurement.requirement.pojo.entity.BiddingTrialDetail;
import org.springblade.procurement.requirement.pojo.entity.PurchaseRequirementDetail;
import org.springblade.procurement.requirement.pojo.entity.Requirement;
import org.springblade.procurement.requirement.pojo.entity.RequirementFile;
import org.springblade.procurement.requirement.pojo.vo.RequirementVO;
import org.springblade.procurement.requirement.service.IRequirementService;
import org.springblade.flow.dto.CompleteTaskDTO;
import org.springblade.flow.dto.StartProcessDTO;
import org.springblade.flow.feign.IFlowClient;
import org.springblade.flow.vo.ProcessInstanceVO;
import org.springblade.flow.vo.ProcessTaskVO;
import org.springblade.core.tool.api.R;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 采购与竞价需求服务。 */
@Service
@RequiredArgsConstructor
public class RequirementServiceImpl extends ServiceImpl<RequirementMapper, Requirement> implements IRequirementService {

	private static final String PURCHASE_APPROVAL_PROCESS_KEY = "procurementPurchaseRequirementApproval";
	private static final String BIDDING_APPROVAL_PROCESS_KEY = "procurementBiddingRequirementApproval";

	private final PurchaseRequirementDetailMapper purchaseDetailMapper;
	private final BiddingTrialDetailMapper trialDetailMapper;
	private final BiddingScrapDetailMapper scrapDetailMapper;
	private final BiddingPartsDetailMapper partsDetailMapper;
	private final RequirementFileMapper fileMapper;
	private final IFlowClient flowClient;

	@Override
	public IPage<Requirement> page(IPage<Requirement> page, Map<String, Object> params, String type) {
		return page(page, buildQueryWrapper(params, type));
	}

	private LambdaQueryWrapper<Requirement> buildQueryWrapper(Map<String, Object> params, String type) {
		String tenantCode = SecureUtil.getTenantId();
		LambdaQueryWrapper<Requirement> wrapper = Wrappers.lambdaQuery();
		wrapper.eq(Requirement::getTenantCode, tenantCode)
			.orderByDesc(Requirement::getUpdateTime);
		if ("bidding".equalsIgnoreCase(type)) {
			wrapper.eq(Requirement::getBiddingFlag, "1");
		} else {
			wrapper.eq(Requirement::getBiddingFlag, "0");
		}
		String tab = parameter(params, "tab");
		if ("processed".equalsIgnoreCase(tab)) {
			addProcessedFilter(wrapper, type, false, tenantCode);
		} else if ("unprocessed".equalsIgnoreCase(tab)) {
			addProcessedFilter(wrapper, type, true, tenantCode);
		}
		String code = parameter(params, "requirementCode", "requirementCode_like");
		if (StringUtils.hasText(code)) {
			wrapper.like(Requirement::getRequirementCode, code);
		}
		String name = parameter(params, "requirementName");
		if (StringUtils.hasText(name)) {
			wrapper.like(Requirement::getRequirementName, name);
		}
		String status = parameter(params, "approvalStatus");
		if (StringUtils.hasText(status)) {
			wrapper.eq(Requirement::getApprovalStatus, status);
		}
		String categoryCode = parameter(params, "categoryCode");
		String categoryName = parameter(params, "categoryName");
		if (StringUtils.hasText(categoryCode) && StringUtils.hasText(categoryName)) {
			wrapper.and(category -> category.eq(Requirement::getCategoryCode, categoryCode)
				.or()
				.eq(Requirement::getCategoryName, categoryName));
		} else if (StringUtils.hasText(categoryCode)) {
			wrapper.eq(Requirement::getCategoryCode, categoryCode);
		} else if (StringUtils.hasText(categoryName)) {
			wrapper.eq(Requirement::getCategoryName, categoryName);
		}
		String updateTimeStart = parameter(params, "updateTimeStart");
		if (StringUtils.hasText(updateTimeStart)) {
			wrapper.ge(Requirement::getUpdateTime, updateTimeStart);
		}
		String updateTimeEnd = parameter(params, "updateTimeEnd");
		if (StringUtils.hasText(updateTimeEnd)) {
			wrapper.le(Requirement::getUpdateTime, updateTimeEnd);
		}
		return wrapper;
	}

	@Override
	public RequirementVO detail(Long id) {
		String tenantCode = SecureUtil.getTenantId();
		Requirement requirement = getOne(Wrappers.<Requirement>lambdaQuery()
			.eq(Requirement::getId, id)
			.eq(Requirement::getTenantCode, tenantCode));
		if (requirement == null) {
			throw new ServiceException("需求不存在或已删除");
		}
		RequirementVO vo = new RequirementVO();
		org.springframework.beans.BeanUtils.copyProperties(requirement, vo);
		List<PurchaseRequirementDetail> purchaseDetails = purchaseDetailMapper.selectList(Wrappers.<PurchaseRequirementDetail>lambdaQuery()
			.eq(PurchaseRequirementDetail::getRequirementId, id)
			.eq(PurchaseRequirementDetail::getTenantCode, tenantCode)
			.eq(PurchaseRequirementDetail::getDelFlag, "0"));
		List<BiddingTrialDetail> trialDetails = trialDetailMapper.selectList(Wrappers.<BiddingTrialDetail>lambdaQuery()
			.eq(BiddingTrialDetail::getRequirementId, id)
			.eq(BiddingTrialDetail::getTenantCode, tenantCode)
			.eq(BiddingTrialDetail::getDelFlag, "0"));
		List<BiddingScrapDetail> scrapDetails = scrapDetailMapper.selectList(Wrappers.<BiddingScrapDetail>lambdaQuery()
			.eq(BiddingScrapDetail::getRequirementId, id)
			.eq(BiddingScrapDetail::getTenantCode, tenantCode)
			.eq(BiddingScrapDetail::getDelFlag, "0"));
		List<BiddingPartsDetail> partsDetails = partsDetailMapper.selectList(Wrappers.<BiddingPartsDetail>lambdaQuery()
			.eq(BiddingPartsDetail::getRequirementId, id)
			.eq(BiddingPartsDetail::getTenantCode, tenantCode)
			.eq(BiddingPartsDetail::getDelFlag, "0"));
		List<RequirementFile> files = fileMapper.selectList(Wrappers.<RequirementFile>lambdaQuery()
			.eq(RequirementFile::getRequirementId, id)
			.eq(RequirementFile::getTenantCode, tenantCode)
			.eq(RequirementFile::getDelFlag, "0"));
		Map<String, List<RequirementFile>> detailFileMap = files.stream()
			.filter(file -> !"0".equals(file.getDetailType()) && file.getDetailId() != null)
			.collect(Collectors.groupingBy(file -> file.getDetailType() + ":" + file.getDetailId()));
		trialDetails.forEach(detail -> detail.setFiles(detailFileMap.getOrDefault("2:" + detail.getId(), List.of())));
		scrapDetails.forEach(detail -> detail.setFiles(detailFileMap.getOrDefault("3:" + detail.getId(), List.of())));
		partsDetails.forEach(detail -> detail.setFiles(detailFileMap.getOrDefault("4:" + detail.getId(), List.of())));
		vo.setPurchaseDetails(purchaseDetails);
		vo.setBiddingTrialDetails(trialDetails);
		vo.setBiddingScrapDetails(scrapDetails);
		vo.setBiddingPartsDetails(partsDetails);
		vo.setFiles(files.stream().filter(file -> "0".equals(file.getDetailType())).toList());
		return vo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean submit(RequirementDTO dto) {
		String tenantCode = SecureUtil.getTenantId();
		String account = SecureUtil.getUserAccount();
		String name = SecureUtil.getUserName();
		Date now = DateUtil.now();
		Requirement target = dto;
		Requirement current = null;
		if (target.getId() != null) {
			current = getOne(Wrappers.<Requirement>lambdaQuery()
				.eq(Requirement::getId, target.getId())
				.eq(Requirement::getTenantCode, tenantCode)
				.eq(Requirement::getDelFlag, "0"));
			if (current == null) {
				throw new ServiceException("需求不存在或已删除");
			}
			target.setBiddingFlag(current.getBiddingFlag());
			if ("0".equals(current.getBiddingFlag()) && !List.of("0", "3").contains(current.getApprovalStatus())) {
				throw new ServiceException("当前状态不允许编辑");
			}
			if ("1".equals(current.getBiddingFlag())) {
				if (!"2".equals(current.getApprovalStatus())) {
					throw new ServiceException("只能补充审核通过的竞价需求");
				}
				copyBiddingReadonlyFields(target, current);
			}
		}
		if (!StringUtils.hasText(target.getRequirementCode())) {
			target.setRequirementCode(generateRequirementCode(tenantCode, "1".equals(target.getBiddingFlag())));
		}
		target.setTenantCode(tenantCode);
		target.setUpdateBy(account);
		target.setUpdateName(name);
		target.setUpdateTime(now);
		target.setDelFlag("0");
		if (!StringUtils.hasText(target.getApprovalStatus())) {
			target.setApprovalStatus("0");
		}
		if (!StringUtils.hasText(target.getBiddingFlag())) {
			target.setBiddingFlag("0");
		}
		// 采购需求页面不编辑目标金额，编辑时保留数据库中的原值，避免空值覆盖。
		if ("0".equals(target.getBiddingFlag()) && current != null) {
			target.setTargetMoney(current.getTargetMoney());
		}
		if ("0".equals(target.getBiddingFlag()) && dto.getPurchaseDetails() != null) {
			target.setBudgetMoney(sumPurchaseMoney(dto.getPurchaseDetails()));
		}
		if (!StringUtils.hasText(target.getRequirementName())) {
			throw new ServiceException("需求名称不能为空");
		}
		boolean saved;
		if (target.getId() == null) {
			target.setCreateBy(account);
			target.setCreateName(name);
			target.setCreateTime(now);
			saved = save(target);
		} else {
			saved = update(target, Wrappers.<Requirement>lambdaUpdate()
				.eq(Requirement::getId, target.getId())
				.eq(Requirement::getTenantCode, tenantCode)
				.eq(Requirement::getDelFlag, "0"));
		}
		if (!saved) {
			return false;
		}
		Long requirementId = target.getId();
		boolean replaceFiles = dto.getFiles() != null;
		if (replaceFiles) {
			fileMapper.delete(Wrappers.<RequirementFile>lambdaQuery()
				.eq(RequirementFile::getRequirementId, requirementId)
				.eq(RequirementFile::getTenantCode, tenantCode));
		}
		if (dto.getPurchaseDetails() != null) {
			removePurchaseDetails(requirementId, tenantCode);
			for (PurchaseRequirementDetail detail : dto.getPurchaseDetails()) {
				if (detail == null) {
					continue;
				}
				prepare(detail, requirementId, tenantCode, account, name, now);
				purchaseDetailMapper.insert(detail);
			}
		}
		if (dto.getBiddingTrialDetails() != null) {
			removeTrialDetails(requirementId, tenantCode);
			for (BiddingTrialDetail detail : dto.getBiddingTrialDetails()) {
				if (detail == null) {
					continue;
				}
				prepare(detail, requirementId, tenantCode, account, name, now);
				trialDetailMapper.insert(detail);
				if (replaceFiles) {
					saveFiles(detail.getFiles(), requirementId, "2", detail.getId(), tenantCode, account, name, now);
				}
			}
		}
		if (dto.getBiddingScrapDetails() != null) {
			removeScrapDetails(requirementId, tenantCode);
			for (BiddingScrapDetail detail : dto.getBiddingScrapDetails()) {
				if (detail == null) {
					continue;
				}
				if (!StringUtils.hasText(detail.getItemCode())) {
					throw new ServiceException("货物编码不能为空");
				}
				prepare(detail, requirementId, tenantCode, account, name, now);
				scrapDetailMapper.insert(detail);
				if (replaceFiles) {
					saveFiles(detail.getFiles(), requirementId, "3", detail.getId(), tenantCode, account, name, now);
				}
			}
		}
		if (dto.getBiddingPartsDetails() != null) {
			removePartsDetails(requirementId, tenantCode);
			for (BiddingPartsDetail detail : dto.getBiddingPartsDetails()) {
				if (detail == null) {
					continue;
				}
				if (!StringUtils.hasText(detail.getItemCode())) {
					throw new ServiceException("物料编号不能为空");
				}
				prepare(detail, requirementId, tenantCode, account, name, now);
				partsDetailMapper.insert(detail);
				if (replaceFiles) {
					saveFiles(detail.getFiles(), requirementId, "4", detail.getId(), tenantCode, account, name, now);
				}
			}
		}
		if (replaceFiles) {
			saveFiles(dto.getFiles(), requirementId, "0", null, tenantCode, account, name, now);
		}
		if ("1".equals(target.getApprovalStatus())
			&& (current == null || "0".equals(current.getApprovalStatus()) || "3".equals(current.getApprovalStatus()))) {
			target.setProcessInstanceId(startApprovalProcess(target));
			target.setUpdateBy(account);
			target.setUpdateName(name);
			target.setUpdateTime(DateUtil.now());
			if (!updateById(target)) {
				throw new ServiceException("采购需求审批流程发起后，需求状态更新失败");
			}
		}
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean receiveFromOa(RequirementDTO dto) {
		return receiveFromOa(dto, "0");
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean receiveFromOaReview(RequirementDTO dto) {
		return receiveFromOa(dto, "1");
	}

	private boolean receiveFromOa(RequirementDTO dto, String approvalStatus) {
		if (dto == null || !StringUtils.hasText(dto.getSourceBusinessCode())) {
			throw new ServiceException("OA业务单号不能为空");
		}
		dto.setId(null);
		dto.setRequirementSource("OA");
		dto.setBiddingFlag("1");
		dto.setApprovalStatus(approvalStatus);
		return submit(dto);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean submitReview(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			throw new ServiceException("请选择需要提交的采购需求");
		}
		String tenantCode = SecureUtil.getTenantId();
		List<Requirement> requirements = list(Wrappers.<Requirement>lambdaQuery()
			.eq(Requirement::getTenantCode, tenantCode)
			.eq(Requirement::getBiddingFlag, "0")
			.eq(Requirement::getApprovalStatus, "0")
			.in(Requirement::getId, ids));
		if (requirements.size() != ids.size()) {
			throw new ServiceException("只能提交草稿状态的采购需求");
		}
		for (Requirement requirement : requirements) {
			String processInstanceId = startApprovalProcess(requirement);
			requirement.setApprovalStatus("1");
			requirement.setProcessInstanceId(processInstanceId);
			requirement.setUpdateBy(SecureUtil.getUserAccount());
			requirement.setUpdateName(SecureUtil.getUserName());
			requirement.setUpdateTime(DateUtil.now());
			updateById(requirement);
		}
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean prepareApproval(Long id) {
		Requirement requirement = getTenantRequirement(id);
		if (!"1".equals(requirement.getApprovalStatus())) {
			throw new ServiceException("当前需求不在审核中");
		}
		if (StringUtils.hasText(requirement.getProcessInstanceId())) {
			return true;
		}
		requirement.setProcessInstanceId(startApprovalProcess(requirement));
		requirement.setUpdateBy(SecureUtil.getUserAccount());
		requirement.setUpdateName(SecureUtil.getUserName());
		requirement.setUpdateTime(DateUtil.now());
		return updateById(requirement);
	}

	private String startApprovalProcess(Requirement requirement) {
		Map<String, Object> variables = new HashMap<>();
		variables.put("requirementId", requirement.getId().toString());
		variables.put("requirementCode", requirement.getRequirementCode());
		variables.put("requirementName", requirement.getRequirementName());
		StartProcessDTO start = new StartProcessDTO();
		start.setProcessDefinitionKey("1".equals(requirement.getBiddingFlag())
			? BIDDING_APPROVAL_PROCESS_KEY
			: PURCHASE_APPROVAL_PROCESS_KEY);
		String businessKey = "procurement:requirement:" + requirement.getId();
		if (StringUtils.hasText(requirement.getProcessInstanceId())) {
			businessKey += ":resubmit:" + DateUtil.now().getTime();
		}
		start.setBusinessKey(businessKey);
		start.setVariables(variables);
		R<ProcessInstanceVO> result = flowClient.start(start);
		if (!R.isSuccess(result) || result.getData() == null) {
			throw new ServiceException(result == null ? "采购需求审批流程发起失败" : result.getMsg());
		}
		return result.getData().getId();
	}

	@Override
	public ProcessTaskVO currentApprovalTask(Long id) {
		Requirement requirement = getTenantRequirement(id);
		if (!"1".equals(requirement.getApprovalStatus()) || !StringUtils.hasText(requirement.getProcessInstanceId())) {
			return null;
		}
		R<ProcessTaskVO> result = flowClient.currentTask(requirement.getProcessInstanceId());
		return R.isSuccess(result) ? result.getData() : null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean approve(Long id, String comment) {
		return finishApproval(id, true, comment);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean reject(Long id, String comment) {
		return finishApproval(id, false, comment);
	}

	private boolean finishApproval(Long id, boolean approved, String comment) {
		Requirement requirement = getTenantRequirement(id);
		if (!"1".equals(requirement.getApprovalStatus()) || !StringUtils.hasText(requirement.getProcessInstanceId())) {
			throw new ServiceException("当前需求不在审核中");
		}
		R<ProcessTaskVO> taskResult = flowClient.currentTask(requirement.getProcessInstanceId());
		if (!R.isSuccess(taskResult) || taskResult.getData() == null) {
			throw new ServiceException("当前用户不是该需求的审批人");
		}
		CompleteTaskDTO complete = new CompleteTaskDTO();
		complete.setTaskId(taskResult.getData().getId());
		complete.setComment(comment);
		complete.setVariables(Map.of("approved", approved));
		R<Void> completeResult = flowClient.complete(complete);
		if (!R.isSuccess(completeResult)) {
			throw new ServiceException(completeResult.getMsg());
		}
		R<ProcessInstanceVO> instanceResult = flowClient.instance(requirement.getProcessInstanceId());
		boolean processCompleted = R.isSuccess(instanceResult)
			&& instanceResult.getData() != null
			&& "COMPLETED".equals(instanceResult.getData().getState());
		requirement.setApprovalStatus(processCompleted ? (approved ? "2" : "3") : "1");
		requirement.setUpdateBy(SecureUtil.getUserAccount());
		requirement.setUpdateName(SecureUtil.getUserName());
		requirement.setUpdateTime(DateUtil.now());
		return updateById(requirement);
	}

	private Requirement getTenantRequirement(Long id) {
		Requirement requirement = getOne(Wrappers.<Requirement>lambdaQuery()
			.eq(Requirement::getId, id)
			.eq(Requirement::getTenantCode, SecureUtil.getTenantId())
			.eq(Requirement::getDelFlag, "0"));
		if (requirement == null) {
			throw new ServiceException("需求不存在或已删除");
		}
		return requirement;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean cancel(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			throw new ServiceException("请选择需要取消的需求");
		}
		String tenantCode = SecureUtil.getTenantId();
		List<Requirement> requirements = list(Wrappers.<Requirement>lambdaQuery()
			.eq(Requirement::getTenantCode, tenantCode)
			.eq(Requirement::getApprovalStatus, "2")
			.in(Requirement::getId, ids));
		if (requirements.size() != ids.size()) {
			throw new ServiceException("只能取消审核通过且已处理的需求");
		}
		Set<Long> processedRequirementIds = processedRequirementIds(ids, tenantCode);
		if (!processedRequirementIds.containsAll(ids)) {
			throw new ServiceException("只能取消审核通过且已处理的需求");
		}
		purchaseDetailMapper.update(null, Wrappers.<PurchaseRequirementDetail>lambdaUpdate()
			.set(PurchaseRequirementDetail::getPackageNo, null)
			.eq(PurchaseRequirementDetail::getTenantCode, tenantCode)
			.in(PurchaseRequirementDetail::getRequirementId, ids));
		trialDetailMapper.update(null, Wrappers.<BiddingTrialDetail>lambdaUpdate()
			.set(BiddingTrialDetail::getPackageNo, null)
			.eq(BiddingTrialDetail::getTenantCode, tenantCode)
			.in(BiddingTrialDetail::getRequirementId, ids));
		scrapDetailMapper.update(null, Wrappers.<BiddingScrapDetail>lambdaUpdate()
			.set(BiddingScrapDetail::getPackageNo, null)
			.eq(BiddingScrapDetail::getTenantCode, tenantCode)
			.in(BiddingScrapDetail::getRequirementId, ids));
		partsDetailMapper.update(null, Wrappers.<BiddingPartsDetail>lambdaUpdate()
			.set(BiddingPartsDetail::getPackageNo, null)
			.eq(BiddingPartsDetail::getTenantCode, tenantCode)
			.in(BiddingPartsDetail::getRequirementId, ids));
		update(Wrappers.<Requirement>lambdaUpdate()
			.set(Requirement::getProcessProjectName, null)
			.set(Requirement::getProcessEngineerBy, null)
			.set(Requirement::getProcessEngineerName, null)
			.set(Requirement::getProcessLeaderBy, null)
			.set(Requirement::getProcessLeaderName, null)
			.set(Requirement::getProcessPurchaseMethod, null)
			.set(Requirement::getProcessProjectType, null)
			.set(Requirement::getProcessRemark, null)
			.eq(Requirement::getTenantCode, tenantCode)
			.in(Requirement::getId, ids));
		return update(Wrappers.<Requirement>lambdaUpdate()
			.set(Requirement::getUpdateBy, SecureUtil.getUserAccount())
			.set(Requirement::getUpdateName, SecureUtil.getUserName())
			.set(Requirement::getUpdateTime, DateUtil.now())
			.eq(Requirement::getTenantCode, tenantCode)
			.in(Requirement::getId, ids));
	}

	@Override
	public List<RequirementExportExcel> export(Map<String, Object> params, String type) {
		return list(buildQueryWrapper(params, type)).stream()
			.map(requirement -> toExportExcel(requirement, type))
			.toList();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean process(RequirementPackageDTO dto) {
		if (dto.getItems() == null || dto.getItems().isEmpty()) {
			throw new ServiceException("请选择需要处理的需求明细");
		}
		if (!StringUtils.hasText(dto.getProjectName())) {
			throw new ServiceException("请输入招采项目名称");
		}
		if (!StringUtils.hasText(dto.getProcurementEngineerName())) {
			throw new ServiceException("请选择招采工程师");
		}
		if (!StringUtils.hasText(dto.getProjectLeaderName())) {
			throw new ServiceException("请选择项目负责人");
		}
		if (!StringUtils.hasText(dto.getPurchaseMethod())) {
			throw new ServiceException("请选择采购方式");
		}
		String tenantCode = SecureUtil.getTenantId();
		List<Long> requirementIds = dto.getItems().stream()
			.map(item -> resolveRequirementId(dto, item))
			.filter(java.util.Objects::nonNull)
			.distinct()
			.toList();
		if (requirementIds.isEmpty()) {
			throw new ServiceException("请选择需要处理的需求");
		}
		List<Requirement> requirements = list(Wrappers.<Requirement>lambdaQuery()
			.eq(Requirement::getTenantCode, tenantCode)
			.eq(Requirement::getApprovalStatus, "2")
			.in(Requirement::getId, requirementIds));
		if (requirements.size() != requirementIds.size()) {
			throw new ServiceException("只能处理审核通过的需求");
		}
		Set<String> categoryCodes = requirements.stream()
			.map(requirement -> StringUtils.hasText(requirement.getCategoryCode())
				? requirement.getCategoryCode() : requirement.getCategoryName())
			.filter(StringUtils::hasText)
			.collect(Collectors.toSet());
		if (categoryCodes.size() > 1) {
			throw new ServiceException("只能选择同一品类的需求");
		}
		boolean bidding = "bidding".equalsIgnoreCase(dto.getType());
		String expectedBiddingFlag = bidding ? "1" : "0";
		if (requirements.stream().anyMatch(requirement -> !expectedBiddingFlag.equals(requirement.getBiddingFlag()))) {
			throw new ServiceException("需求类型与处理页面不一致");
		}
		if (bidding != isBiddingPurchaseMethod(dto.getPurchaseMethod())) {
			throw new ServiceException("采购方式与需求类型不匹配");
		}
		Map<Long, List<RequirementPackageDTO.PackageItem>> itemsByRequirement = new HashMap<>();
		Set<String> submittedItemKeys = new HashSet<>();
		for (RequirementPackageDTO.PackageItem item : dto.getItems()) {
			if (item == null) {
				throw new ServiceException("需求明细信息不完整");
			}
			if (item.getDetailId() == null || !StringUtils.hasText(item.getDetailType())) {
				throw new ServiceException("需求明细信息不完整");
			}
			if (!StringUtils.hasText(item.getPackageNo())) {
				throw new ServiceException("请填写所有明细的包号");
			}
			if ((!bidding && !"1".equals(item.getDetailType()))
				|| (bidding && !List.of("2", "3", "4").contains(item.getDetailType()))) {
				throw new ServiceException("需求明细类型与处理页面不一致");
			}
			Long requirementId = item.getRequirementId() == null ? dto.getRequirementId() : item.getRequirementId();
			if (requirementId == null) {
				throw new ServiceException("需求明细缺少需求主表ID");
			}
			String itemKey = requirementId + ":" + item.getDetailType() + ":" + item.getDetailId();
			if (!submittedItemKeys.add(itemKey)) {
				throw new ServiceException("需求明细不能重复选择");
			}
			itemsByRequirement.computeIfAbsent(requirementId, ignored -> new ArrayList<>()).add(item);
		}
		for (Map.Entry<Long, List<RequirementPackageDTO.PackageItem>> entry : itemsByRequirement.entrySet()) {
			Set<String> detailTypes = entry.getValue().stream()
				.map(RequirementPackageDTO.PackageItem::getDetailType)
				.collect(Collectors.toSet());
			if (detailTypes.size() != 1) {
				throw new ServiceException("同一需求只能处理一种明细类型");
			}
			String detailType = detailTypes.iterator().next();
			Set<Long> submittedDetailIds = entry.getValue().stream()
				.map(RequirementPackageDTO.PackageItem::getDetailId)
				.collect(Collectors.toSet());
			Set<Long> activeDetailIds = activeDetailIds(entry.getKey(), detailType, tenantCode);
			if (!activeDetailIds.equals(submittedDetailIds)) {
				throw new ServiceException("请完整选择该需求的全部明细");
			}
		}
		for (RequirementPackageDTO.PackageItem item : dto.getItems()) {
			Long requirementId = resolveRequirementId(dto, item);
			String packageNo = item.getPackageNo().trim();
			int updatedRows = switch (item.getDetailType()) {
				case "1" -> purchaseDetailMapper.update(null, Wrappers.<PurchaseRequirementDetail>lambdaUpdate()
					.set(PurchaseRequirementDetail::getPackageNo, packageNo)
					.eq(PurchaseRequirementDetail::getId, item.getDetailId())
					.eq(PurchaseRequirementDetail::getRequirementId, requirementId)
					.eq(PurchaseRequirementDetail::getTenantCode, tenantCode)
					.eq(PurchaseRequirementDetail::getDelFlag, "0"));
				case "2" -> trialDetailMapper.update(null, Wrappers.<BiddingTrialDetail>lambdaUpdate()
					.set(BiddingTrialDetail::getPackageNo, packageNo)
					.eq(BiddingTrialDetail::getId, item.getDetailId())
					.eq(BiddingTrialDetail::getRequirementId, requirementId)
					.eq(BiddingTrialDetail::getTenantCode, tenantCode)
					.eq(BiddingTrialDetail::getDelFlag, "0"));
				case "3" -> scrapDetailMapper.update(null, Wrappers.<BiddingScrapDetail>lambdaUpdate()
					.set(BiddingScrapDetail::getPackageNo, packageNo)
					.eq(BiddingScrapDetail::getId, item.getDetailId())
					.eq(BiddingScrapDetail::getRequirementId, requirementId)
					.eq(BiddingScrapDetail::getTenantCode, tenantCode)
					.eq(BiddingScrapDetail::getDelFlag, "0"));
				case "4" -> partsDetailMapper.update(null, Wrappers.<BiddingPartsDetail>lambdaUpdate()
					.set(BiddingPartsDetail::getPackageNo, packageNo)
					.eq(BiddingPartsDetail::getId, item.getDetailId())
					.eq(BiddingPartsDetail::getRequirementId, requirementId)
					.eq(BiddingPartsDetail::getTenantCode, tenantCode)
					.eq(BiddingPartsDetail::getDelFlag, "0"));
				default -> throw new ServiceException("未知的需求明细类型");
			};
			if (updatedRows != 1) {
				throw new ServiceException("需求明细不存在或已删除");
			}
		}
		for (Long requirementId : requirementIds) {
			updateProcessFields(requirementId, dto, tenantCode);
		}
		return true;
	}

	private Long resolveRequirementId(RequirementPackageDTO dto, RequirementPackageDTO.PackageItem item) {
		return item == null || item.getRequirementId() == null ? dto.getRequirementId() : item.getRequirementId();
	}

	private boolean isBiddingPurchaseMethod(String purchaseMethod) {
		return "3".equals(purchaseMethod) || "4".equals(purchaseMethod);
	}

	private void updateProcessFields(Long requirementId, RequirementPackageDTO dto, String tenantCode) {
		update(Wrappers.<Requirement>lambdaUpdate()
			.set(Requirement::getProcessProjectName, dto.getProjectName())
			.set(Requirement::getProcessEngineerBy, dto.getProcurementEngineerBy())
			.set(Requirement::getProcessEngineerName, dto.getProcurementEngineerName())
			.set(Requirement::getProcessLeaderBy, dto.getProjectLeaderBy())
			.set(Requirement::getProcessLeaderName, dto.getProjectLeaderName())
			.set(Requirement::getProcessPurchaseMethod, dto.getPurchaseMethod())
			.set(Requirement::getProcessProjectType, dto.getProjectType())
			.set(Requirement::getProcessRemark, dto.getRemark())
			.set(Requirement::getUpdateBy, SecureUtil.getUserAccount())
			.set(Requirement::getUpdateName, SecureUtil.getUserName())
			.set(Requirement::getUpdateTime, DateUtil.now())
			.eq(Requirement::getTenantCode, tenantCode)
			.eq(Requirement::getId, requirementId));
	}

	private Set<Long> activeDetailIds(Long requirementId, String detailType, String tenantCode) {
		return switch (detailType) {
			case "1" -> purchaseDetailMapper.selectList(Wrappers.<PurchaseRequirementDetail>lambdaQuery()
				.select(PurchaseRequirementDetail::getId)
				.eq(PurchaseRequirementDetail::getRequirementId, requirementId)
				.eq(PurchaseRequirementDetail::getTenantCode, tenantCode)
				.eq(PurchaseRequirementDetail::getDelFlag, "0"))
				.stream().map(PurchaseRequirementDetail::getId).collect(Collectors.toSet());
			case "2" -> trialDetailMapper.selectList(Wrappers.<BiddingTrialDetail>lambdaQuery()
				.select(BiddingTrialDetail::getId)
				.eq(BiddingTrialDetail::getRequirementId, requirementId)
				.eq(BiddingTrialDetail::getTenantCode, tenantCode)
				.eq(BiddingTrialDetail::getDelFlag, "0"))
				.stream().map(BiddingTrialDetail::getId).collect(Collectors.toSet());
			case "3" -> scrapDetailMapper.selectList(Wrappers.<BiddingScrapDetail>lambdaQuery()
				.select(BiddingScrapDetail::getId)
				.eq(BiddingScrapDetail::getRequirementId, requirementId)
				.eq(BiddingScrapDetail::getTenantCode, tenantCode)
				.eq(BiddingScrapDetail::getDelFlag, "0"))
				.stream().map(BiddingScrapDetail::getId).collect(Collectors.toSet());
			case "4" -> partsDetailMapper.selectList(Wrappers.<BiddingPartsDetail>lambdaQuery()
				.select(BiddingPartsDetail::getId)
				.eq(BiddingPartsDetail::getRequirementId, requirementId)
				.eq(BiddingPartsDetail::getTenantCode, tenantCode)
				.eq(BiddingPartsDetail::getDelFlag, "0"))
				.stream().map(BiddingPartsDetail::getId).collect(Collectors.toSet());
			default -> throw new ServiceException("未知的需求明细类型");
		};
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean deleteLogic(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return false;
		}
		String tenantCode = SecureUtil.getTenantId();
		return update(Wrappers.<Requirement>lambdaUpdate()
			.set(Requirement::getDelFlag, "1")
			.eq(Requirement::getTenantCode, tenantCode)
			.in(Requirement::getApprovalStatus, "0", "3")
			.in(Requirement::getId, ids));
	}

	private void removePurchaseDetails(Long id, String tenantCode) {
		purchaseDetailMapper.delete(Wrappers.<PurchaseRequirementDetail>lambdaQuery().eq(PurchaseRequirementDetail::getRequirementId, id).eq(PurchaseRequirementDetail::getTenantCode, tenantCode));
	}

	private BigDecimal sumPurchaseMoney(List<PurchaseRequirementDetail> details) {
		if (details == null || details.isEmpty()) {
			return BigDecimal.ZERO;
		}
		return details.stream()
			.filter(Objects::nonNull)
			.map(PurchaseRequirementDetail::getBudgetMoney)
			.filter(Objects::nonNull)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	private Set<Long> processedRequirementIds(List<Long> ids, String tenantCode) {
		Set<Long> processedIds = new HashSet<>();
		purchaseDetailMapper.selectList(Wrappers.<PurchaseRequirementDetail>lambdaQuery()
			.select(PurchaseRequirementDetail::getRequirementId)
			.in(PurchaseRequirementDetail::getRequirementId, ids)
			.eq(PurchaseRequirementDetail::getTenantCode, tenantCode)
			.eq(PurchaseRequirementDetail::getDelFlag, "0")
			.isNotNull(PurchaseRequirementDetail::getPackageNo)
			.ne(PurchaseRequirementDetail::getPackageNo, ""))
			.forEach(detail -> processedIds.add(detail.getRequirementId()));
		trialDetailMapper.selectList(Wrappers.<BiddingTrialDetail>lambdaQuery()
			.select(BiddingTrialDetail::getRequirementId)
			.in(BiddingTrialDetail::getRequirementId, ids)
			.eq(BiddingTrialDetail::getTenantCode, tenantCode)
			.eq(BiddingTrialDetail::getDelFlag, "0")
			.isNotNull(BiddingTrialDetail::getPackageNo)
			.ne(BiddingTrialDetail::getPackageNo, ""))
			.forEach(detail -> processedIds.add(detail.getRequirementId()));
		scrapDetailMapper.selectList(Wrappers.<BiddingScrapDetail>lambdaQuery()
			.select(BiddingScrapDetail::getRequirementId)
			.in(BiddingScrapDetail::getRequirementId, ids)
			.eq(BiddingScrapDetail::getTenantCode, tenantCode)
			.eq(BiddingScrapDetail::getDelFlag, "0")
			.isNotNull(BiddingScrapDetail::getPackageNo)
			.ne(BiddingScrapDetail::getPackageNo, ""))
			.forEach(detail -> processedIds.add(detail.getRequirementId()));
		partsDetailMapper.selectList(Wrappers.<BiddingPartsDetail>lambdaQuery()
			.select(BiddingPartsDetail::getRequirementId)
			.in(BiddingPartsDetail::getRequirementId, ids)
			.eq(BiddingPartsDetail::getTenantCode, tenantCode)
			.eq(BiddingPartsDetail::getDelFlag, "0")
			.isNotNull(BiddingPartsDetail::getPackageNo)
			.ne(BiddingPartsDetail::getPackageNo, ""))
			.forEach(detail -> processedIds.add(detail.getRequirementId()));
		return processedIds;
	}

	private void copyBiddingReadonlyFields(Requirement target, Requirement current) {
		target.setRequirementCode(current.getRequirementCode());
		target.setRequirementName(current.getRequirementName());
		target.setBudgetMoney(current.getBudgetMoney());
		target.setTargetMoney(current.getTargetMoney());
		target.setApprovalStatus(current.getApprovalStatus());
		target.setBiddingFlag(current.getBiddingFlag());
		target.setPurchaseMethodSuggestion(current.getPurchaseMethodSuggestion());
		target.setProcurementEngineerBy(current.getProcurementEngineerBy());
		target.setProcurementEngineerName(current.getProcurementEngineerName());
		target.setProjectLeaderBy(current.getProjectLeaderBy());
		target.setProjectLeaderName(current.getProjectLeaderName());
		target.setRequirementDeptCode(current.getRequirementDeptCode());
		target.setRequirementDeptName(current.getRequirementDeptName());
		target.setRequirementSource(current.getRequirementSource());
		target.setSourceBusinessCode(current.getSourceBusinessCode());
		target.setTechnicalRequirement(current.getTechnicalRequirement());
		target.setRecommendedSupplier(current.getRecommendedSupplier());
		target.setProcessInstanceId(current.getProcessInstanceId());
		target.setRemark(current.getRemark());
		target.setFields1(current.getFields1());
		target.setFields2(current.getFields2());
		target.setCreateBy(current.getCreateBy());
		target.setCreateName(current.getCreateName());
		target.setCreateTime(current.getCreateTime());
	}

	private String generateRequirementCode(String tenantCode, boolean bidding) {
		String prefix = bidding ? "JJXQ" : "CGXQ";
		String base = prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
		for (int index = 0; index < 100; index++) {
			String code = base + String.format("%02d", index);
			long count = count(Wrappers.<Requirement>lambdaQuery()
				.eq(Requirement::getTenantCode, tenantCode)
				.eq(Requirement::getRequirementCode, code)
				.eq(Requirement::getDelFlag, "0"));
			if (count == 0) {
				return code;
			}
		}
		throw new ServiceException("需求编号生成失败，请稍后重试");
	}

	private RequirementExportExcel toExportExcel(Requirement requirement, String type) {
		RequirementExportExcel excel = new RequirementExportExcel();
		excel.setRequirementCode(requirement.getRequirementCode());
		excel.setRequirementName(requirement.getRequirementName());
		excel.setCategoryName(requirement.getCategoryName());
		BigDecimal amount = "bidding".equalsIgnoreCase(type) ? requirement.getTargetMoney() : requirement.getBudgetMoney();
		excel.setAmount(amount == null ? null : amount.toPlainString());
		excel.setApprovalStatus(statusLabel(requirement.getApprovalStatus()));
		excel.setPurchaseMethodSuggestion(purchaseMethodLabel(requirement.getPurchaseMethodSuggestion()));
		excel.setProcurementEngineerName(requirement.getProcurementEngineerName());
		excel.setProjectLeaderName(requirement.getProjectLeaderName());
		excel.setRequirementDeptName(requirement.getRequirementDeptName());
		excel.setRequirementSource(requirement.getRequirementSource());
		excel.setCreateName(requirement.getCreateName());
		excel.setCreateTime(requirement.getCreateTime());
		excel.setUpdateName(requirement.getUpdateName());
		excel.setUpdateTime(requirement.getUpdateTime());
		return excel;
	}

	private String statusLabel(String status) {
		if (!StringUtils.hasText(status)) {
			return "";
		}
		return switch (status) {
			case "0" -> "草稿";
			case "1" -> "审核中";
			case "2" -> "审核通过";
			case "3" -> "审核驳回";
			default -> status;
		};
	}

	private String purchaseMethodLabel(String method) {
		if (!StringUtils.hasText(method)) {
			return "";
		}
		return switch (method) {
			case "1" -> "公开招标";
			case "2" -> "邀请招标";
			case "3" -> "公开竞价";
			case "4" -> "邀请竞价";
			case "5" -> "询比价";
			case "6" -> "比价/定向";
			default -> method;
		};
	}
	private void removeTrialDetails(Long id, String tenantCode) {
		trialDetailMapper.delete(Wrappers.<BiddingTrialDetail>lambdaQuery().eq(BiddingTrialDetail::getRequirementId, id).eq(BiddingTrialDetail::getTenantCode, tenantCode));
	}
	private void removeScrapDetails(Long id, String tenantCode) {
		scrapDetailMapper.delete(Wrappers.<BiddingScrapDetail>lambdaQuery().eq(BiddingScrapDetail::getRequirementId, id).eq(BiddingScrapDetail::getTenantCode, tenantCode));
	}
	private void removePartsDetails(Long id, String tenantCode) {
		partsDetailMapper.delete(Wrappers.<BiddingPartsDetail>lambdaQuery().eq(BiddingPartsDetail::getRequirementId, id).eq(BiddingPartsDetail::getTenantCode, tenantCode));
	}

	private void prepare(PurchaseRequirementDetail detail, Long id, String tenant, String account, String name, Date now) {
		detail.setId(null); detail.setRequirementId(id); detail.setTenantCode(tenant); detail.setDelFlag("0"); detail.setCreateBy(account); detail.setCreateName(name); detail.setCreateTime(now); detail.setUpdateBy(account); detail.setUpdateName(name); detail.setUpdateTime(now);
	}
	private void prepare(BiddingTrialDetail detail, Long id, String tenant, String account, String name, Date now) {
		detail.setId(null); detail.setRequirementId(id); detail.setTenantCode(tenant); detail.setDelFlag("0"); detail.setCreateBy(account); detail.setCreateName(name); detail.setCreateTime(now); detail.setUpdateBy(account); detail.setUpdateName(name); detail.setUpdateTime(now);
	}
	private void prepare(BiddingScrapDetail detail, Long id, String tenant, String account, String name, Date now) {
		detail.setId(null); detail.setRequirementId(id); detail.setTenantCode(tenant); detail.setDelFlag("0"); detail.setCreateBy(account); detail.setCreateName(name); detail.setCreateTime(now); detail.setUpdateBy(account); detail.setUpdateName(name); detail.setUpdateTime(now);
	}
	private void prepare(BiddingPartsDetail detail, Long id, String tenant, String account, String name, Date now) {
		detail.setId(null); detail.setRequirementId(id); detail.setTenantCode(tenant); detail.setDelFlag("0"); detail.setCreateBy(account); detail.setCreateName(name); detail.setCreateTime(now); detail.setUpdateBy(account); detail.setUpdateName(name); detail.setUpdateTime(now);
	}

	private void saveFiles(List<RequirementFile> files, Long requirementId, String detailType, Long detailId,
		String tenantCode, String account, String name, Date now) {
		if (files == null) {
			return;
		}
		for (RequirementFile file : files) {
			file.setId(null);
			file.setRequirementId(requirementId);
			file.setDetailType(detailType);
			file.setDetailId(detailId);
			file.setTenantCode(tenantCode);
			file.setDelFlag("0");
			file.setCreateBy(account);
			file.setCreateName(name);
			file.setCreateTime(now);
			file.setUpdateBy(account);
			file.setUpdateName(name);
			file.setUpdateTime(now);
			fileMapper.insert(file);
		}
	}

	private String parameter(Map<String, Object> params, String... names) {
		for (String name : names) {
			Object value = params.get(name);
			if (value != null && StringUtils.hasText(value.toString())) {
				return value.toString().trim();
			}
		}
		return null;
	}

	private void addProcessedFilter(LambdaQueryWrapper<Requirement> wrapper, String type,
		boolean unprocessed, String tenantCode) {
		String purchase = "SELECT 1 FROM proc_purchase_requirement_detail d WHERE d.requirement_id = proc_requirement.id AND d.tenant_code = {0} AND d.del_flag = '0' AND d.package_no IS NOT NULL AND d.package_no <> ''";
		String trial = "SELECT 1 FROM proc_bidding_trial_detail d WHERE d.requirement_id = proc_requirement.id AND d.tenant_code = {0} AND d.del_flag = '0' AND d.package_no IS NOT NULL AND d.package_no <> ''";
		String scrap = "SELECT 1 FROM proc_bidding_scrap_detail d WHERE d.requirement_id = proc_requirement.id AND d.tenant_code = {0} AND d.del_flag = '0' AND d.package_no IS NOT NULL AND d.package_no <> ''";
		String parts = "SELECT 1 FROM proc_bidding_parts_detail d WHERE d.requirement_id = proc_requirement.id AND d.tenant_code = {0} AND d.del_flag = '0' AND d.package_no IS NOT NULL AND d.package_no <> ''";
		if ("bidding".equalsIgnoreCase(type)) {
			String expression = unprocessed
				? "NOT EXISTS (" + trial + ") AND NOT EXISTS (" + scrap + ") AND NOT EXISTS (" + parts + ")"
				: "(EXISTS (" + trial + ") OR EXISTS (" + scrap + ") OR EXISTS (" + parts + "))";
			wrapper.apply(expression, tenantCode);
		} else {
			wrapper.apply((unprocessed ? "NOT EXISTS (" : "EXISTS (") + purchase + ")", tenantCode);
		}
	}
}
