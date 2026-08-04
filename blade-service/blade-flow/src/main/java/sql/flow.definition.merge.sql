-- 将独立“流程定义”菜单合并到“模型设计”。
-- 本脚本用于已经执行过旧版 flow.menu.sql 的数据库，可重复执行。

SET NAMES utf8mb4;

DELETE FROM `blade_role_menu`
WHERE `menu_id` = '2084200000000000002';

DELETE FROM `blade_menu`
WHERE `id` = '2084200000000000002';

UPDATE `blade_menu`
SET `parent_id` = '2084200000000000011',
    `code` = 'flow_model_definition_download',
    `name` = '下载部署版本',
    `alias` = 'download',
    `path` = '/api/blade-flow/process/definition/resource',
    `source` = 'download',
    `sort` = 7,
    `action` = 7,
    `remark` = NULL,
    `is_deleted` = 0
WHERE `id` = '2084200000000000006';

UPDATE `blade_menu`
SET `parent_id` = '2084200000000000011',
    `code` = 'flow_model_definition_state',
    `name` = '部署状态管理',
    `alias` = 'state',
    `path` = '/api/blade-flow/process/definition/suspend',
    `source` = 'setting',
    `sort` = 6,
    `action` = 6,
    `remark` = '包含挂起与激活',
    `is_deleted` = 0
WHERE `id` = '2084200000000000007';

INSERT INTO `blade_menu`(
    `id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`,
    `category`, `action`, `is_open`, `remark`, `is_deleted`
)
VALUES (
    '2084200000000000022', '2084200000000000011',
    'flow_model_definition_delete', '删除部署版本', 'delete',
    '/api/blade-flow/process/definition/remove', 'delete', 8,
    2, 8, 1, NULL, 0
)
ON DUPLICATE KEY UPDATE
    `parent_id` = VALUES(`parent_id`),
    `code` = VALUES(`code`),
    `name` = VALUES(`name`),
    `alias` = VALUES(`alias`),
    `path` = VALUES(`path`),
    `source` = VALUES(`source`),
    `sort` = VALUES(`sort`),
    `category` = VALUES(`category`),
    `action` = VALUES(`action`),
    `is_open` = VALUES(`is_open`),
    `remark` = VALUES(`remark`),
    `is_deleted` = VALUES(`is_deleted`);

INSERT INTO `blade_role_menu`(`id`, `menu_id`, `role_id`)
VALUES ('2084201000000000022', '2084200000000000022', 1123598816738675201)
ON DUPLICATE KEY UPDATE
    `menu_id` = VALUES(`menu_id`),
    `role_id` = VALUES(`role_id`);
