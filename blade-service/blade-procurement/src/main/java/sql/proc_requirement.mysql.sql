-- Modifications Copyright (c) 2026, fingerheart521 (daoguangliu@qq.com).
-- 采购需求与竞价需求建表脚本

SET NAMES utf8mb4;

-- ----------------------------
-- 统一需求主表
-- ----------------------------
CREATE TABLE `proc_requirement` (
  `id` bigint NOT NULL COMMENT '主键',
  `requirement_code` varchar(50) NOT NULL COMMENT '需求编号',
  `requirement_name` varchar(100) DEFAULT NULL COMMENT '需求名称',
  `category_code` varchar(50) DEFAULT NULL COMMENT '品类编号，字典：proc_requirement_category',
  `category_name` varchar(100) DEFAULT NULL COMMENT '品类名称',
  `budget_money` decimal(18,2) DEFAULT NULL COMMENT '预算金额（元），采购需求列表展示',
  `target_money` decimal(18,2) DEFAULT NULL COMMENT '目标金额（元），竞价需求列表展示，可与预算金额同时存在',
  `approval_status` varchar(2) NOT NULL DEFAULT '0' COMMENT '审批状态；0草稿，1审核中，2审核通过，3审核驳回',
  `bidding_flag` char(1) NOT NULL DEFAULT '0' COMMENT '竞价场景标记：0否，1是',
  `purchase_method_suggestion` varchar(2) DEFAULT NULL COMMENT '采购方式建议',
  `procurement_engineer_by` varchar(20) DEFAULT NULL COMMENT '招采工程师账号',
  `procurement_engineer_name` varchar(50) DEFAULT NULL COMMENT '招采工程师名称',
  `project_leader_by` varchar(20) DEFAULT NULL COMMENT '项目负责人账号',
  `project_leader_name` varchar(50) DEFAULT NULL COMMENT '项目负责人名称',
  `requirement_dept_code` varchar(50) DEFAULT NULL COMMENT '需求部门编号',
  `requirement_dept_name` varchar(100) DEFAULT NULL COMMENT '需求部门名称',
  `requirement_source` varchar(20) DEFAULT NULL COMMENT '需求来源；PMS、OA、ERP、SRM',
  `source_business_code` varchar(100) DEFAULT NULL COMMENT '来源系统业务唯一标识，用于接口幂等',
  `technical_requirement` varchar(255) DEFAULT NULL COMMENT '技术要求',
  `recommended_supplier` varchar(255) DEFAULT NULL COMMENT '推荐供应商说明',
  `process_instance_id` varchar(50) DEFAULT NULL COMMENT '审批流程实例ID',
  `process_project_name` varchar(100) DEFAULT NULL COMMENT '需求处理时的招采项目名称',
  `process_engineer_by` varchar(20) DEFAULT NULL COMMENT '需求处理时的招采工程师账号',
  `process_engineer_name` varchar(50) DEFAULT NULL COMMENT '需求处理时的招采工程师名称',
  `process_leader_by` varchar(20) DEFAULT NULL COMMENT '需求处理时的项目负责人账号',
  `process_leader_name` varchar(50) DEFAULT NULL COMMENT '需求处理时的项目负责人名称',
  `process_purchase_method` varchar(2) DEFAULT NULL COMMENT '需求处理时的采购方式',
  `process_project_type` varchar(50) DEFAULT NULL COMMENT '需求处理时的项目类型',
  `process_remark` varchar(255) DEFAULT NULL COMMENT '需求处理时的备注',
  `fields1` varchar(255) DEFAULT NULL COMMENT '预留字段1',
  `fields2` varchar(255) DEFAULT NULL COMMENT '预留字段2',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建人账号',
  `create_name` varchar(50) DEFAULT NULL COMMENT '创建人昵称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新人账号',
  `update_name` varchar(50) DEFAULT NULL COMMENT '更新人昵称',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_code` varchar(10) NOT NULL COMMENT '租户编号',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`),
  -- 保证租户内有效需求编号唯一
  UNIQUE KEY `uk_proc_requirement_active_code`
    (`tenant_code`, (CASE WHEN `del_flag` = '0' THEN `requirement_code` ELSE NULL END)),
  -- 保证外部来源业务唯一，用于接口幂等
  UNIQUE KEY `uk_proc_requirement_source`
    (`tenant_code`, `requirement_source`, `source_business_code`),
  CONSTRAINT `ck_proc_requirement_budget_money` CHECK (`budget_money` >= 0),
  CONSTRAINT `ck_proc_requirement_target_money` CHECK (`target_money` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购与竞价统一需求主表';

-- ----------------------------
-- 普通采购需求完整明细
-- ----------------------------
CREATE TABLE `proc_purchase_requirement_detail` (
  `id` bigint NOT NULL COMMENT '主键',
  `requirement_id` bigint NOT NULL COMMENT '需求主表ID',
  `package_no` varchar(50) DEFAULT NULL COMMENT '包号，需求处理时填写',
  `project_approval_code` varchar(50) DEFAULT NULL COMMENT '立项编号',
  `project_approval_name` varchar(100) DEFAULT NULL COMMENT '立项名称',
  `item_code` varchar(50) DEFAULT NULL COMMENT '货物编码',
  `item_name` varchar(100) DEFAULT NULL COMMENT '货物名称',
  `qty` decimal(18,3) DEFAULT 0.000 COMMENT '需求数量',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `tax_rate` decimal(8,4) DEFAULT NULL COMMENT '税率（%）',
  `budget_money` decimal(18,2) DEFAULT NULL COMMENT '明细预算金额（元）',
  `fields1` varchar(255) DEFAULT NULL COMMENT '预留字段1',
  `fields2` varchar(255) DEFAULT NULL COMMENT '预留字段2',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建人账号',
  `create_name` varchar(50) DEFAULT NULL COMMENT '创建人昵称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新人账号',
  `update_name` varchar(50) DEFAULT NULL COMMENT '更新人昵称',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_code` varchar(10) NOT NULL COMMENT '租户编号',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`),
  CONSTRAINT `ck_proc_purchase_requirement_qty` CHECK (`qty` >= 0),
  CONSTRAINT `ck_proc_purchase_requirement_budget_money` CHECK (`budget_money` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='普通采购需求完整明细';

-- ----------------------------
-- 折价招标试制试验类零部件总成竞价单
-- ----------------------------
CREATE TABLE `proc_bidding_trial_detail` (
  `id` bigint NOT NULL COMMENT '主键',
  `requirement_id` bigint NOT NULL COMMENT '需求主表ID',
  `package_no` varchar(50) DEFAULT NULL COMMENT '分包序号（包号），需求处理时填写',
  `item_name` varchar(100) DEFAULT NULL COMMENT '物资名称',
  `model` varchar(100) DEFAULT NULL COMMENT '型号',
  `emission_standard` varchar(50) DEFAULT NULL COMMENT '排放',
  `engine_brand` varchar(100) DEFAULT NULL COMMENT '发动机品牌',
  `engine_power` varchar(50) DEFAULT NULL COMMENT '发动机马力',
  `production_time` datetime DEFAULT NULL COMMENT '生产时间',
  `trial_time` datetime DEFAULT NULL COMMENT '试制时间',
  `brand` varchar(100) DEFAULT NULL COMMENT '品牌',
  `qty` decimal(18,3) DEFAULT 0.000 COMMENT '数量',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `applicable_model` varchar(100) DEFAULT NULL COMMENT '适用机型',
  `fields1` varchar(255) DEFAULT NULL COMMENT '预留字段1',
  `fields2` varchar(255) DEFAULT NULL COMMENT '预留字段2',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建人账号',
  `create_name` varchar(50) DEFAULT NULL COMMENT '创建人昵称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新人账号',
  `update_name` varchar(50) DEFAULT NULL COMMENT '更新人昵称',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_code` varchar(10) NOT NULL COMMENT '租户编号',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`),
  CONSTRAINT `ck_proc_bidding_trial_qty` CHECK (`qty` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='折价招标试制试验类零部件总成竞价单';

-- ----------------------------
-- 破损类废旧物资竞价完整明细
-- ----------------------------
CREATE TABLE `proc_bidding_scrap_detail` (
  `id` bigint NOT NULL COMMENT '主键',
  `requirement_id` bigint NOT NULL COMMENT '需求主表ID',
  `package_no` varchar(50) DEFAULT NULL COMMENT '分包序号（包号），需求处理时填写',
  `item_code` varchar(50) NOT NULL COMMENT '物资编码',
  `item_name` varchar(100) DEFAULT NULL COMMENT '物资名称',
  `agreement_period` varchar(100) DEFAULT NULL COMMENT '协议周期',
  `qty` decimal(18,3) DEFAULT 0.000 COMMENT '数量',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `material_description` text COMMENT '物资简介',
  `pricing_rule` varchar(255) DEFAULT NULL COMMENT '定价规则',
  `settlement_method` varchar(255) DEFAULT NULL COMMENT '结算方式',
  `fields1` varchar(255) DEFAULT NULL COMMENT '预留字段1',
  `fields2` varchar(255) DEFAULT NULL COMMENT '预留字段2',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建人账号',
  `create_name` varchar(50) DEFAULT NULL COMMENT '创建人昵称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新人账号',
  `update_name` varchar(50) DEFAULT NULL COMMENT '更新人昵称',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_code` varchar(10) NOT NULL COMMENT '租户编号',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`),
  CONSTRAINT `ck_proc_bidding_scrap_qty` CHECK (`qty` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='破损废旧物资竞价完整明细';

-- ----------------------------
-- 折价招标配件类竞价完整明细
-- ----------------------------
CREATE TABLE `proc_bidding_parts_detail` (
  `id` bigint NOT NULL COMMENT '主键',
  `requirement_id` bigint NOT NULL COMMENT '需求主表ID',
  `package_no` varchar(50) DEFAULT NULL COMMENT '分包序号（包号），需求处理时填写',
  `item_code` varchar(50) NOT NULL COMMENT '物料编号',
  `item_name` varchar(100) DEFAULT NULL COMMENT '物料名称',
  `drawcode` varchar(50) DEFAULT NULL COMMENT '图号',
  `qty` decimal(18,3) DEFAULT 0.000 COMMENT '数量',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `applicable_model` varchar(100) DEFAULT NULL COMMENT '适用机型',
  `fields1` varchar(255) DEFAULT NULL COMMENT '预留字段1',
  `fields2` varchar(255) DEFAULT NULL COMMENT '预留字段2',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建人账号',
  `create_name` varchar(50) DEFAULT NULL COMMENT '创建人昵称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新人账号',
  `update_name` varchar(50) DEFAULT NULL COMMENT '更新人昵称',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_code` varchar(10) NOT NULL COMMENT '租户编号',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`),
  CONSTRAINT `ck_proc_bidding_parts_qty` CHECK (`qty` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='折价配件类竞价完整明细';

-- ----------------------------
-- 需求附件关系
-- ----------------------------
CREATE TABLE `proc_requirement_file` (
  `id` bigint NOT NULL COMMENT '主键',
  `requirement_id` bigint NOT NULL COMMENT '需求主表ID',
  `resource_id` bigint DEFAULT NULL COMMENT '统一文件服务资源ID',
  `detail_type` varchar(2) NOT NULL DEFAULT '0' COMMENT '附件归属类型；0需求主表，1普通采购，2试制试验，3破损废旧物资，4折价配件',
  `detail_id` bigint DEFAULT NULL COMMENT '对应完整明细表ID，主表附件为空',
  `file_name` varchar(100) NOT NULL COMMENT '附件名称',
  `file_url` varchar(255) NOT NULL COMMENT '附件地址',
  `file_type` varchar(20) DEFAULT NULL COMMENT '附件类型或扩展名',
  `file_size` bigint DEFAULT NULL COMMENT '附件大小，单位字节',
  `fields1` varchar(255) DEFAULT NULL COMMENT '预留字段1',
  `fields2` varchar(255) DEFAULT NULL COMMENT '预留字段2',
  `create_by` varchar(20) DEFAULT NULL COMMENT '创建人账号',
  `create_name` varchar(50) DEFAULT NULL COMMENT '创建人昵称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(20) DEFAULT NULL COMMENT '更新人账号',
  `update_name` varchar(50) DEFAULT NULL COMMENT '更新人昵称',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tenant_code` varchar(10) NOT NULL COMMENT '租户编号',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '删除标记：0正常，1删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='采购与竞价需求附件关系';
