/*
  将 pitb_review_expert.professional_codes 从逗号分隔字符串迁移为 JSON 数组。

  示例：
  T00001          -> ["T00001"]
  T00001,T00002   -> ["T00001", "T00002"]

  请先在目标数据库备份 pitb_review_expert 表，再执行本脚本。
*/

UPDATE `pitb_review_expert`
SET `professional_codes` = CASE
  WHEN `professional_codes` IS NULL OR TRIM(`professional_codes`) = '' THEN NULL
  ELSE CONCAT(
    '["',
    REPLACE(
      REPLACE(
        REPLACE(TRIM(`professional_codes`), '\\', '\\\\'),
        '"', '\\"'
      ),
      ',', '","'
    ),
    '"]'
  )
END;

ALTER TABLE `pitb_review_expert`
  MODIFY COLUMN `professional_codes` JSON NULL COMMENT '专业标签编码集合，JSON数组';
