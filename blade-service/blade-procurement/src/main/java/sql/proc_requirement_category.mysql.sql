-- Copyright (c) 2026, fingerheart521 (daoguangliu@qq.com).
-- 采购需求与竞价需求品类字典初始化

SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;

START TRANSACTION;

SET @category_root_id = COALESCE(
  (
    SELECT `id`
    FROM `blade_dict`
    WHERE `code` = 'proc_requirement_category'
      AND `parent_id` = 0
      AND `dict_key` = -1
    LIMIT 1
  ),
  CAST(CONV(SUBSTRING(MD5('dict:proc_requirement_category'), 1, 15), 16, 10) AS UNSIGNED)
);

INSERT INTO `blade_dict`
  (`id`, `parent_id`, `code`, `dict_key`, `dict_value`, `sort`, `remark`, `is_deleted`)
SELECT @category_root_id, 0, 'proc_requirement_category', -1, '采购需求品类', 14, NULL, 0
WHERE NOT EXISTS (
  SELECT 1
  FROM `blade_dict`
  WHERE `code` = 'proc_requirement_category'
    AND `parent_id` = 0
    AND `dict_key` = -1
);

UPDATE `blade_dict`
SET `parent_id` = 0,
    `code` = 'proc_requirement_category',
    `dict_key` = -1,
    `dict_value` = '采购需求品类',
    `sort` = 14,
    `is_deleted` = 0
WHERE `id` = @category_root_id;

SET @trial_category_id = CAST(CONV(SUBSTRING(MD5('dict:proc_requirement_category:2'), 1, 15), 16, 10) AS UNSIGNED);
SET @scrap_category_id = CAST(CONV(SUBSTRING(MD5('dict:proc_requirement_category:3'), 1, 15), 16, 10) AS UNSIGNED);
SET @parts_category_id = CAST(CONV(SUBSTRING(MD5('dict:proc_requirement_category:4'), 1, 15), 16, 10) AS UNSIGNED);

INSERT INTO `blade_dict`
  (`id`, `parent_id`, `code`, `dict_key`, `dict_value`, `sort`, `remark`, `is_deleted`)
SELECT @trial_category_id, @category_root_id, 'proc_requirement_category', 2,
       '试制试验类零部件总成', 1, NULL, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `blade_dict`
  WHERE `code` = 'proc_requirement_category' AND `dict_key` = 2
);

INSERT INTO `blade_dict`
  (`id`, `parent_id`, `code`, `dict_key`, `dict_value`, `sort`, `remark`, `is_deleted`)
SELECT @scrap_category_id, @category_root_id, 'proc_requirement_category', 3,
       '破损类废旧物资', 2, NULL, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `blade_dict`
  WHERE `code` = 'proc_requirement_category' AND `dict_key` = 3
);

INSERT INTO `blade_dict`
  (`id`, `parent_id`, `code`, `dict_key`, `dict_value`, `sort`, `remark`, `is_deleted`)
SELECT @parts_category_id, @category_root_id, 'proc_requirement_category', 4,
       '折价配件类', 3, NULL, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `blade_dict`
  WHERE `code` = 'proc_requirement_category' AND `dict_key` = 4
);

UPDATE `blade_dict`
SET `parent_id` = @category_root_id,
    `code` = 'proc_requirement_category',
    `dict_value` = CASE `dict_key`
      WHEN 2 THEN '试制试验类零部件总成'
      WHEN 3 THEN '破损类废旧物资'
      WHEN 4 THEN '折价配件类'
    END,
    `sort` = CASE `dict_key`
      WHEN 2 THEN 1
      WHEN 3 THEN 2
      WHEN 4 THEN 3
    END,
    `is_deleted` = 0
WHERE `code` = 'proc_requirement_category'
  AND `dict_key` IN (2, 3, 4);

COMMIT;
