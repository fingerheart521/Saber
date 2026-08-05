-- 招采服务、基础信息与评标专家库菜单初始化
-- 支持已有菜单：按 code 复用原记录并修正层级、名称与路由。

SET @procurement_menu_id = (
  SELECT `id` FROM `blade_menu`
  WHERE `code` = 'procurement' AND `is_deleted` = 0
  LIMIT 1
);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT
  2080108367517487001, 0, 'procurement', '招采服务', 'menu', '/procurement',
  'iconfont iconicon_order', 10, 1, 0, 1, '招采服务一级菜单', 0
WHERE @procurement_menu_id IS NULL;

SET @procurement_menu_id = COALESCE(@procurement_menu_id, 2080108367517487001);

UPDATE `blade_menu`
SET `parent_id` = 0,
    `name` = '招采服务',
    `alias` = 'menu',
    `path` = '/procurement',
    `category` = 1,
    `action` = 0,
    `is_open` = 1,
    `is_deleted` = 0
WHERE `id` = @procurement_menu_id;

SET @procurement_base_menu_id = (
  SELECT `id` FROM `blade_menu`
  WHERE `code` = 'procurement_base_info' AND `is_deleted` = 0
  LIMIT 1
);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT
  2080108367517487002, @procurement_menu_id, 'procurement_base_info', '基础信息', 'menu',
  '/procurement/base-info', 'iconfont iconicon_addresslist', 1, 1, 0, 1, '招采基础信息目录', 0
WHERE @procurement_base_menu_id IS NULL;

SET @procurement_base_menu_id = COALESCE(@procurement_base_menu_id, 2080108367517487002);

UPDATE `blade_menu`
SET `parent_id` = @procurement_menu_id,
    `name` = '基础信息',
    `alias` = 'menu',
    `path` = '/procurement/base-info',
    `category` = 1,
    `action` = 0,
    `is_open` = 1,
    `is_deleted` = 0
WHERE `id` = @procurement_base_menu_id;

SET @review_expert_menu_id = (
  SELECT `id` FROM `blade_menu`
  WHERE `code` = 'reviewexpert' AND `is_deleted` = 0
  LIMIT 1
);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT
  2080108367517487105, @procurement_base_menu_id, 'reviewexpert', '评标专家库', 'menu',
  '/procurement/review-expert', 'iconfont iconicon_setting', 10, 1, 0, 1, NULL, 0
WHERE @review_expert_menu_id IS NULL;

SET @review_expert_menu_id = COALESCE(@review_expert_menu_id, 2080108367517487105);

UPDATE `blade_menu`
SET `parent_id` = @procurement_base_menu_id,
    `name` = '评标专家库',
    `alias` = 'menu',
    `path` = '/procurement/review-expert',
    `category` = 1,
    `action` = 0,
    `is_open` = 1,
    `is_deleted` = 0
WHERE `id` = @review_expert_menu_id;

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487106, @review_expert_menu_id, 'reviewexpert_add', '新增', 'add',
       '/procurement/review-expert/add', 'plus', 1, 2, 1, 1, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'reviewexpert_add' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487107, @review_expert_menu_id, 'reviewexpert_edit', '修改', 'edit',
       '/procurement/review-expert/edit', 'form', 2, 2, 2, 1, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'reviewexpert_edit' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487108, @review_expert_menu_id, 'reviewexpert_delete', '删除', 'delete',
       '/api/blade-procurement/review-expert/remove', 'delete', 3, 2, 3, 1, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'reviewexpert_delete' AND `is_deleted` = 0);

INSERT INTO `blade_menu`
  (`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
SELECT 2080108367517487109, @review_expert_menu_id, 'reviewexpert_view', '查看', 'view',
       '/procurement/review-expert/view', 'file-text', 4, 2, 2, 1, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM `blade_menu` WHERE `code` = 'reviewexpert_view' AND `is_deleted` = 0);

UPDATE `blade_menu`
SET `parent_id` = @review_expert_menu_id,
    `path` = CASE `code`
      WHEN 'reviewexpert_add' THEN '/procurement/review-expert/add'
      WHEN 'reviewexpert_edit' THEN '/procurement/review-expert/edit'
      WHEN 'reviewexpert_delete' THEN '/api/blade-procurement/review-expert/remove'
      WHEN 'reviewexpert_view' THEN '/procurement/review-expert/view'
      ELSE `path`
    END,
    `is_deleted` = 0
WHERE `code` IN ('reviewexpert_add', 'reviewexpert_edit', 'reviewexpert_delete', 'reviewexpert_view');

-- 已拥有评标专家库权限的角色自动补齐两个上级目录权限。
INSERT INTO `blade_role_menu` (`id`, `menu_id`, `role_id`)
SELECT
  CAST(CONV(SUBSTRING(MD5(CONCAT(`role_menu`.`role_id`, ':', @procurement_menu_id)), 1, 15), 16, 10) AS UNSIGNED),
  @procurement_menu_id,
  `role_menu`.`role_id`
FROM `blade_role_menu` `role_menu`
WHERE `role_menu`.`menu_id` = @review_expert_menu_id
  AND NOT EXISTS (
    SELECT 1 FROM `blade_role_menu` `existing_role_menu`
    WHERE `existing_role_menu`.`role_id` = `role_menu`.`role_id`
      AND `existing_role_menu`.`menu_id` = @procurement_menu_id
  );

INSERT INTO `blade_role_menu` (`id`, `menu_id`, `role_id`)
SELECT
  CAST(CONV(SUBSTRING(MD5(CONCAT(`role_menu`.`role_id`, ':', @procurement_base_menu_id)), 1, 15), 16, 10) AS UNSIGNED),
  @procurement_base_menu_id,
  `role_menu`.`role_id`
FROM `blade_role_menu` `role_menu`
WHERE `role_menu`.`menu_id` = @review_expert_menu_id
  AND NOT EXISTS (
    SELECT 1 FROM `blade_role_menu` `existing_role_menu`
    WHERE `existing_role_menu`.`role_id` = `role_menu`.`role_id`
      AND `existing_role_menu`.`menu_id` = @procurement_base_menu_id
  );
