/*
  评标专家库规范版建表脚本

  依据：2.项目开发规范-命名篇V1.0.md
  业务字段来源优先级：
  1. 评标专家库页面可见表格字段；
  2. 页面 Avue 配置中的隐藏表单、流程、职称及附件字段；
  3. 列表与详情接口返回字段；
  4. 旧版 pitb_review_expert(1).sql 仅用于交叉核对。

  注意：
  1. 本脚本不包含 DROP TABLE，避免误删现有数据。
  2. 本规范使用 create_by、tenant_code、del_flag 等字段，和 SpringBlade
     BaseEntity 默认的 create_user、tenant_id、is_deleted 字段不兼容。
  3. projected_num（招采次数）、review_num（评审次数）和隐藏的
     expert_projected_list（参与招标项目）均为关联查询结果，不在专家主表重复存储。
  4. expert_code_sel、expert_file_name_list、expert_title_list 等为前端/DTO
     辅助字段，不直接映射为主表字段。
  5. 页面源码中已注释停用的 work_unit、major_class、expert_type、
     category_code、category_name 不再进入新表。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 评标专家库
-- 命名结构：招采模块 proc + 功能 review_expert
-- ----------------------------
CREATE TABLE `proc_review_expert` (
  `id` bigint NOT NULL COMMENT '主键，雪花ID',
  `expert_code` varchar(20) NOT NULL COMMENT '专家账号',
  `expert_name` varchar(50) NOT NULL COMMENT '专家姓名',
  `expert_source_type` varchar(2) NOT NULL COMMENT '专家来源类型，字典：proc_review_expert_source',
  `sex` varchar(2) DEFAULT NULL COMMENT '性别，字典：sys_user_sex',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `major_years` varchar(64) DEFAULT NULL COMMENT '本专业年限',
  `professional_codes` json DEFAULT NULL COMMENT '专业标签编码集合，JSON数组',
  `dept_code` varchar(50) DEFAULT NULL COMMENT '部门编号',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '部门名称',
  `department_code` varchar(50) DEFAULT NULL COMMENT '科室编号',
  `department_name` varchar(100) DEFAULT NULL COMMENT '科室名称',
  `approval_status` varchar(2) NOT NULL DEFAULT '0' COMMENT '审批状态：0未审批，1准入审批中，2准入驳回，3已准入，4清退审批中，5清退驳回，6已清退',
  `admission_description` varchar(300) DEFAULT NULL COMMENT '准入说明',
  `retirement_description` varchar(300) DEFAULT NULL COMMENT '清退说明',
  `dept_leader_by` varchar(20) DEFAULT NULL COMMENT '部门一把手账号',
  `dept_leader_name` varchar(50) DEFAULT NULL COMMENT '部门一把手名称',
  `hr_approver_by` varchar(20) DEFAULT NULL COMMENT '人力资源审批人账号',
  `hr_approver_name` varchar(50) DEFAULT NULL COMMENT '人力资源审批人名称',
  `enable_status` varchar(2) NOT NULL DEFAULT 'Y' COMMENT '启用状态：Y启用，N停用',
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
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_proc_review_expert_active_code`
    (`tenant_code`, (CASE WHEN `del_flag` = '0' THEN `expert_code` ELSE NULL END)) USING BTREE,
  KEY `idx_proc_review_expert_code_history` (`tenant_code`, `expert_code`, `del_flag`) USING BTREE,
  KEY `idx_proc_review_expert_approval` (`tenant_code`, `approval_status`, `del_flag`) USING BTREE,
  KEY `idx_proc_review_expert_dept` (`tenant_code`, `dept_code`, `department_code`, `del_flag`) USING BTREE,
  KEY `idx_proc_review_expert_create_time` (`tenant_code`, `create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评标专家库';

-- ----------------------------
-- 评标专家取得职称明细
-- ----------------------------
CREATE TABLE `proc_review_expert_title` (
  `id` bigint NOT NULL COMMENT '主键，雪花ID',
  `expert_id` bigint NOT NULL COMMENT '专家主表ID',
  `title_name` varchar(100) NOT NULL COMMENT '职称名称',
  `obtain_date` datetime DEFAULT NULL COMMENT '取得日期',
  `certificate_code` varchar(255) DEFAULT NULL COMMENT '证书编号',
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
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_proc_review_expert_title_expert` (`tenant_code`, `expert_id`, `del_flag`) USING BTREE,
  KEY `idx_proc_review_expert_title_date` (`tenant_code`, `obtain_date`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评标专家取得职称明细';

-- ----------------------------
-- 评标专家附件关系
-- 文件本体由统一文件服务保存，本表只保存关联信息
-- ----------------------------
CREATE TABLE `proc_review_expert_file` (
  `id` bigint NOT NULL COMMENT '主键，雪花ID',
  `expert_id` bigint NOT NULL COMMENT '专家主表ID',
  `resource_id` bigint DEFAULT NULL COMMENT '统一文件服务资源ID',
  `file_name` varchar(100) NOT NULL COMMENT '附件名称',
  `file_url` varchar(500) NOT NULL COMMENT '附件地址',
  `file_type` varchar(20) DEFAULT NULL COMMENT '附件类型',
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
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_proc_review_expert_file_expert` (`tenant_code`, `expert_id`, `del_flag`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评标专家附件关系';

SET FOREIGN_KEY_CHECKS = 1;

