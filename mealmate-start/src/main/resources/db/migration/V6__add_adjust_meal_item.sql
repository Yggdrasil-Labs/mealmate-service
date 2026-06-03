ALTER TABLE `meal_plan_item`
  ADD COLUMN `is_manually_adjusted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已手动调整',
  ADD COLUMN `adjust_count` INT NOT NULL DEFAULT 0 COMMENT '累计调整次数';

CREATE TABLE IF NOT EXISTS `meal_plan_item_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `item_id` BIGINT NOT NULL,
  `plan_id` BIGINT NOT NULL,
  `old_recipe_id` BIGINT DEFAULT NULL,
  `new_recipe_id` BIGINT NOT NULL,
  `adjust_reason` VARCHAR(64) DEFAULT NULL,
  `adjusted_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `adjusted_by` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_item_id` (`item_id`),
  KEY `idx_plan_id_date` (`plan_id`, `adjusted_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='餐次调整历史表';
