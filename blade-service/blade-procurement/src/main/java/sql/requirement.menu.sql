-- Copyright (c) 2026, fingerheart521 (daoguangliu@qq.com).
-- 采购需求与竞价需求菜单初始化

SET @procurement_menu_id = (
  SELECT `id` FROM `blade_menu`
  WHERE `code` = 'procurement' AND `is_deleted` = 0
  LIMIT 1
);

SET @requirement_menu_id = (
  SELECT `id` FROM `blade_menu`
  WHERE `code` = 'procurement_requirement' AND `is_deleted` = 0
  LIMIT 1
);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT
  2080108367517487200, @procurement_menu_id, 'procurement_requirement', '采购需求', 'menu',
  '/procurement/requirement', 'iconfont iconicon_order', 2, 1, 0, 1, NULL, 0
WHERE @procurement_menu_id IS NOT NULL AND @requirement_menu_id IS NULL;

SET @requirement_menu_id = COALESCE(@requirement_menu_id, 2080108367517487200);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT
  2080108367517487201, @requirement_menu_id, 'purchase_requirement', '采购需求', 'menu',
  '/procurement/requirement/purchase', 'iconfont iconicon_order', 1, 1, 0, 1, NULL, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `blade_menu` WHERE `code` = 'purchase_requirement' AND `is_deleted` = 0
);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT
  2080108367517487202, @requirement_menu_id, 'bidding_requirement', '竞价需求', 'menu',
  '/procurement/requirement/bidding', 'iconfont iconicon_order', 2, 1, 0, 1, NULL, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `blade_menu` WHERE `code` = 'bidding_requirement' AND `is_deleted` = 0
);

SET @purchase_requirement_id = (
  SELECT `id` FROM `blade_menu` WHERE `code` = 'purchase_requirement' AND `is_deleted` = 0 LIMIT 1
);
SET @bidding_requirement_id = (
  SELECT `id` FROM `blade_menu` WHERE `code` = 'bidding_requirement' AND `is_deleted` = 0 LIMIT 1
);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487203, @purchase_requirement_id, 'purchase_requirement_add', '新增', 'add',
       '/procurement/requirement/purchase/add', 'plus', 1, 2, 1, 1, NULL, 0
WHERE @purchase_requirement_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'purchase_requirement_add' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487204, @purchase_requirement_id, 'purchase_requirement_edit', '修改', 'edit',
       '/procurement/requirement/purchase/edit', 'form', 2, 2, 2, 1, NULL, 0
WHERE @purchase_requirement_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'purchase_requirement_edit' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487205, @purchase_requirement_id, 'purchase_requirement_delete', '删除', 'delete',
       '/api/blade-procurement/requirement/remove', 'delete', 3, 2, 3, 1, NULL, 0
WHERE @purchase_requirement_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'purchase_requirement_delete' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487206, @purchase_requirement_id, 'purchase_requirement_process', '需求处理', 'process',
       '/procurement/requirement/purchase-process', 'setting', 4, 2, 2, 1, NULL, 0
WHERE @purchase_requirement_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'purchase_requirement_process' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487207, @bidding_requirement_id, 'bidding_requirement_supplement', '补充明细', 'edit',
       '/procurement/requirement/bidding-supplement', 'form', 1, 2, 2, 1, NULL, 0
WHERE @bidding_requirement_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'bidding_requirement_supplement' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487208, @bidding_requirement_id, 'bidding_requirement_process', '需求处理', 'process',
       '/procurement/requirement/bidding-process', 'setting', 2, 2, 2, 1, NULL, 0
WHERE @bidding_requirement_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'bidding_requirement_process' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487209, @purchase_requirement_id, 'purchase_requirement_export', '导出', 'export',
       '/api/blade-procurement/requirement/export', 'download', 5, 2, 1, 1, NULL, 0
WHERE @purchase_requirement_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'purchase_requirement_export' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487210, @purchase_requirement_id, 'purchase_requirement_submit', '提交', 'submit',
       '/api/blade-procurement/requirement/submit-review', 'check', 6, 2, 2, 1, NULL, 0
WHERE @purchase_requirement_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'purchase_requirement_submit' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487211, @purchase_requirement_id, 'purchase_requirement_cancel', '取消', 'cancel',
       '/api/blade-procurement/requirement/cancel', 'close', 7, 2, 2, 1, NULL, 0
WHERE @purchase_requirement_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'purchase_requirement_cancel' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487212, @bidding_requirement_id, 'bidding_requirement_export', '导出', 'export',
       '/api/blade-procurement/requirement/export', 'download', 3, 2, 1, 1, NULL, 0
WHERE @bidding_requirement_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'bidding_requirement_export' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487213, @bidding_requirement_id, 'bidding_requirement_cancel', '取消', 'cancel',
       '/api/blade-procurement/requirement/cancel', 'close', 4, 2, 2, 1, NULL, 0
WHERE @bidding_requirement_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'bidding_requirement_cancel' AND `is_deleted` = 0);
