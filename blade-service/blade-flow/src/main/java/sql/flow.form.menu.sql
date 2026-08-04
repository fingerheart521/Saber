-- 表单设计增量菜单，可在已经执行过 flow.menu.sql 的数据库中重复执行。
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000017', '2084200000000000001', 'flow_form', '表单设计', 'menu', '/flow/form', 'iconfont iconicon_doc', 2, 1, 0, 1, NULL, 0)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `path` = VALUES(`path`), `source` = VALUES(`source`), `sort` = VALUES(`sort`), `is_deleted` = 0;

INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000018', '2084200000000000017', 'flow_form_add', '新增表单', 'add', '/api/blade-flow/process/form/save', 'plus', 1, 2, 1, 1, NULL, 0)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `path` = VALUES(`path`), `is_deleted` = 0;

INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000019', '2084200000000000017', 'flow_form_edit', '修改表单', 'edit', '/api/blade-flow/process/form/save', 'edit', 2, 2, 2, 1, NULL, 0)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `path` = VALUES(`path`), `is_deleted` = 0;

INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000020', '2084200000000000017', 'flow_form_design', '设计表单', 'design', '/api/blade-flow/process/form/save-design', 'setting', 3, 2, 3, 1, NULL, 0)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `path` = VALUES(`path`), `is_deleted` = 0;

INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000021', '2084200000000000017', 'flow_form_delete', '删除表单', 'delete', '/api/blade-flow/process/form/remove', 'delete', 4, 2, 4, 1, NULL, 0)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `path` = VALUES(`path`), `is_deleted` = 0;

INSERT IGNORE INTO `blade_role_menu`(`id`, `menu_id`, `role_id`) VALUES
('2084201000000000017', '2084200000000000017', 1123598816738675201),
('2084201000000000018', '2084200000000000018', 1123598816738675201),
('2084201000000000019', '2084200000000000019', 1123598816738675201),
('2084201000000000020', '2084200000000000020', 1123598816738675201),
('2084201000000000021', '2084200000000000021', 1123598816738675201);
