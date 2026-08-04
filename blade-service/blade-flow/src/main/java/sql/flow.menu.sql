-- 流程管理菜单。默认授权给系统 administrator 角色，其他业务角色请在“权限管理-角色管理”中按需分配。
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000001', 0, 'flow', '流程管理', 'menu', '/flow', 'iconfont iconicon_order', 6, 1, 0, 1, 'Flowable流程中心', 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000011', '2084200000000000001', 'flow_model', '模型设计', 'menu', '/flow/model', 'iconfont iconicon_study', 1, 1, 0, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000017', '2084200000000000001', 'flow_form', '表单设计', 'menu', '/flow/form', 'iconfont iconicon_doc', 2, 1, 0, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000003', '2084200000000000001', 'flow_todo', '我的待办', 'menu', '/flow/todo', 'iconfont iconicon_doc', 3, 1, 0, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000004', '2084200000000000001', 'flow_done', '我的已办', 'menu', '/flow/done', 'iconfont iconicon_savememo', 4, 1, 0, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000005', '2084200000000000001', 'flow_mine', '我发起的', 'menu', '/flow/mine', 'iconfont iconicon_send', 5, 1, 0, 1, NULL, 0);

INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000012', '2084200000000000011', 'flow_model_add', '新增模型', 'add', '/api/blade-flow/process/model/save', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000013', '2084200000000000011', 'flow_model_edit', '修改模型', 'edit', '/api/blade-flow/process/model/save', 'edit', 2, 2, 2, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000014', '2084200000000000011', 'flow_model_design', '设计模型', 'design', '/api/blade-flow/process/model/save-design', 'setting', 3, 2, 3, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000015', '2084200000000000011', 'flow_model_deploy', '部署模型', 'deploy', '/api/blade-flow/process/model/deploy', 'upload', 4, 2, 4, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000016', '2084200000000000011', 'flow_model_delete', '删除模型', 'delete', '/api/blade-flow/process/model/remove', 'delete', 5, 2, 5, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000007', '2084200000000000011', 'flow_model_definition_state', '部署状态管理', 'state', '/api/blade-flow/process/definition/suspend', 'setting', 6, 2, 6, 1, '包含挂起与激活', 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000006', '2084200000000000011', 'flow_model_definition_download', '下载部署版本', 'download', '/api/blade-flow/process/definition/resource', 'download', 7, 2, 7, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000022', '2084200000000000011', 'flow_model_definition_delete', '删除部署版本', 'delete', '/api/blade-flow/process/definition/remove', 'delete', 8, 2, 8, 1, NULL, 0);

INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000018', '2084200000000000017', 'flow_form_add', '新增表单', 'add', '/api/blade-flow/process/form/save', 'plus', 1, 2, 1, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000019', '2084200000000000017', 'flow_form_edit', '修改表单', 'edit', '/api/blade-flow/process/form/save', 'edit', 2, 2, 2, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000020', '2084200000000000017', 'flow_form_design', '设计表单', 'design', '/api/blade-flow/process/form/save-design', 'setting', 3, 2, 3, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000021', '2084200000000000017', 'flow_form_delete', '删除表单', 'delete', '/api/blade-flow/process/form/remove', 'delete', 4, 2, 4, 1, NULL, 0);

INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000008', '2084200000000000003', 'flow_task_claim', '签收任务', 'claim', '/api/blade-flow/process/task/claim', 'user-add', 1, 2, 1, 1, '包含签收与取消签收', 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000009', '2084200000000000003', 'flow_task_complete', '办理任务', 'complete', '/api/blade-flow/process/task/complete', 'check', 2, 2, 2, 1, NULL, 0);
INSERT INTO `blade_menu`(`id`, `parent_id`, `code`, `name`, `alias`, `path`, `source`, `sort`, `category`, `action`, `is_open`, `remark`, `is_deleted`)
VALUES ('2084200000000000010', '2084200000000000005', 'flow_instance_cancel', '撤销流程', 'cancel', '/api/blade-flow/process/instance/cancel', 'close', 1, 2, 3, 1, NULL, 0);

INSERT INTO `blade_role_menu`(`id`, `menu_id`, `role_id`) VALUES
('2084201000000000001', '2084200000000000001', 1123598816738675201),
('2084201000000000002', '2084200000000000022', 1123598816738675201),
('2084201000000000003', '2084200000000000003', 1123598816738675201),
('2084201000000000004', '2084200000000000004', 1123598816738675201),
('2084201000000000005', '2084200000000000005', 1123598816738675201),
('2084201000000000006', '2084200000000000006', 1123598816738675201),
('2084201000000000007', '2084200000000000007', 1123598816738675201),
('2084201000000000008', '2084200000000000008', 1123598816738675201),
('2084201000000000009', '2084200000000000009', 1123598816738675201),
('2084201000000000010', '2084200000000000010', 1123598816738675201),
('2084201000000000011', '2084200000000000011', 1123598816738675201),
('2084201000000000012', '2084200000000000012', 1123598816738675201),
('2084201000000000013', '2084200000000000013', 1123598816738675201),
('2084201000000000014', '2084200000000000014', 1123598816738675201),
('2084201000000000015', '2084200000000000015', 1123598816738675201),
('2084201000000000016', '2084200000000000016', 1123598816738675201);
INSERT INTO `blade_role_menu`(`id`, `menu_id`, `role_id`) VALUES
('2084201000000000017', '2084200000000000017', 1123598816738675201),
('2084201000000000018', '2084200000000000018', 1123598816738675201),
('2084201000000000019', '2084200000000000019', 1123598816738675201),
('2084201000000000020', '2084200000000000020', 1123598816738675201),
('2084201000000000021', '2084200000000000021', 1123598816738675201);
