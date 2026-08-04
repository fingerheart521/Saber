INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2080108367517487105', 1123598815738675201, 'reviewexpert', '', 'menu', '/expert/reviewexpert', NULL, 1, 1, 0, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2080108367517487106', '2080108367517487105', 'reviewexpert_add', '新增', 'add', '/expert/reviewexpert/add', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2080108367517487107', '2080108367517487105', 'reviewexpert_edit', '修改', 'edit', '/expert/reviewexpert/edit', 'form', 2, 2, 2, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2080108367517487108', '2080108367517487105', 'reviewexpert_delete', '删除', 'delete', '/api/blade-expert/reviewexpert/remove', 'delete', 3, 2, 3, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2080108367517487109', '2080108367517487105', 'reviewexpert_view', '查看', 'view', '/expert/reviewexpert/view', 'file-text', 4, 2, 2, 1, NULL, 0);
