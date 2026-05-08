CREATE TABLE IF NOT EXISTS `recipe` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` VARCHAR(128) NOT NULL COMMENT '菜品名称',
  `recipe_type` VARCHAR(32) NOT NULL COMMENT '菜品类型',
  `source_type` VARCHAR(32) NOT NULL COMMENT '来源类型',
  `season_tag` VARCHAR(32) NULL COMMENT '季节标签',
  `crowd_tag` VARCHAR(32) NULL COMMENT '适配人群标签',
  `taste_tag` VARCHAR(512) NULL COMMENT '口味标签（逗号分隔）',
  `difficulty_level` VARCHAR(32) NULL COMMENT '难度等级',
  `cooking_time_min` INT NULL COMMENT '烹饪时长（分钟）',
  `cover_image_url` VARCHAR(512) NULL COMMENT '封面图地址',
  `is_baby_friendly` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否宝宝友好',
  `is_weight_loss_friendly` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否减脂友好',
  `status` VARCHAR(32) NOT NULL COMMENT '菜品状态',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  `deleted` BIGINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记，0 为有效，删除时回填当前主键',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recipe_name_deleted` (`name`, `deleted`),
  KEY `idx_recipe_type_deleted` (`recipe_type`, `deleted`),
  KEY `idx_recipe_season_crowd_deleted` (`season_tag`, `crowd_tag`, `deleted`),
  KEY `idx_recipe_status_deleted` (`status`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品';

CREATE TABLE IF NOT EXISTS `recipe_ingredient` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `recipe_id` BIGINT NOT NULL COMMENT '菜品 ID',
  `ingredient_name` VARCHAR(128) NOT NULL COMMENT '食材名称',
  `ingredient_type` VARCHAR(32) NULL COMMENT '食材类型',
  `quantity` DECIMAL(12, 2) NULL COMMENT '数量',
  `unit` VARCHAR(32) NULL COMMENT '单位',
  `is_main` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否主料',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  KEY `idx_recipe_ingredient_recipe_id` (`recipe_id`),
  KEY `idx_recipe_ingredient_name` (`ingredient_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品食材';

CREATE TABLE IF NOT EXISTS `recipe_step` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `recipe_id` BIGINT NOT NULL COMMENT '菜品 ID',
  `step_no` INT NOT NULL COMMENT '步骤序号',
  `content` TEXT NOT NULL COMMENT '步骤内容',
  `image_url` VARCHAR(512) NULL COMMENT '步骤图片地址',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recipe_step_recipe_id_step_no` (`recipe_id`, `step_no`),
  KEY `idx_recipe_step_recipe_id` (`recipe_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品步骤';

CREATE TABLE IF NOT EXISTS `recipe_nutrition` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `recipe_id` BIGINT NOT NULL COMMENT '菜品 ID',
  `calories` DECIMAL(12, 2) NULL COMMENT '热量',
  `protein` DECIMAL(12, 2) NULL COMMENT '蛋白质',
  `fat` DECIMAL(12, 2) NULL COMMENT '脂肪',
  `carbohydrate` DECIMAL(12, 2) NULL COMMENT '碳水化合物',
  `fiber` DECIMAL(12, 2) NULL COMMENT '膳食纤维',
  `calcium` DECIMAL(12, 2) NULL COMMENT '钙',
  `sodium` DECIMAL(12, 2) NULL COMMENT '钠',
  `nutrition_json` JSON NULL COMMENT '营养扩展 JSON',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` VARCHAR(64) NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NULL COMMENT '更新人',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_recipe_nutrition_recipe_id` (`recipe_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜品营养';
