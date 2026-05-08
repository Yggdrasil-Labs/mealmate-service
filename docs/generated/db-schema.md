最后生成: 2026-05-07

# 数据库结构快照

## 说明

本文件记录 MealMate 当前的数据库表结构，由 Flyway 迁移脚本生成。

**最后更新**：2026-05-08  
**数据库版本**：V4  
**生成方式**：基于 `mealmate-start/src/main/resources/db/migration/` 下的迁移脚本

## 表结构概览

| 表名 | 用途 | 所属上下文 |
| --- | --- | --- |
| `family_profile` | 家庭画像 | Family Context |
| `family_member` | 家庭成员 | Family Context |
| `member_preference` | 成员偏好 | Family Context |
| `recipe` | 菜品 | Recipe Context |
| `recipe_ingredient` | 菜品食材 | Recipe Context |
| `recipe_step` | 菜品步骤 | Recipe Context |
| `recipe_nutrition` | 菜品营养 | Recipe Context |

## Family Context

### family_profile - 家庭画像

**用途**：存储家庭基础信息和整体饮食目标

| 字段 | 类型 | 说明 | 约束 |
| --- | --- | --- | --- |
| `id` | BIGINT | 主键 | PK, AUTO_INCREMENT |
| `family_name` | VARCHAR(128) | 家庭名称 | NOT NULL |
| `family_code` | VARCHAR(64) | 家庭唯一编码 | NOT NULL, UNIQUE |
| `status` | VARCHAR(32) | 家庭状态 | NOT NULL |
| `region` | VARCHAR(64) | 默认地区 | NULL |
| `meal_goal_json` | JSON | 饮食目标 JSON | NULL |
| `remark` | VARCHAR(512) | 备注 | NULL |
| `created_at` | DATETIME | 创建时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | DATETIME | 更新时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE |
| `created_by` | VARCHAR(64) | 创建人 | NULL |
| `updated_by` | VARCHAR(64) | 更新人 | NULL |
| `deleted` | TINYINT(1) | 逻辑删除标记 | NOT NULL, DEFAULT 0 |

**索引**：
- `uk_family_profile_family_code` - 家庭编码唯一索引
- `idx_family_profile_status_deleted` - 状态和删除标记索引

### family_member - 家庭成员

**用途**：存储家庭成员基础信息

| 字段 | 类型 | 说明 | 约束 |
| --- | --- | --- | --- |
| `id` | BIGINT | 主键 | PK, AUTO_INCREMENT |
| `family_id` | BIGINT | 家庭 ID | NOT NULL |
| `name` | VARCHAR(64) | 成员姓名 | NOT NULL |
| `role_type` | VARCHAR(32) | 成员角色类型 | NOT NULL |
| `gender` | VARCHAR(32) | 性别类型 | NULL |
| `birthday` | DATE | 生日 | NULL |
| `region` | VARCHAR(64) | 成员地区 | NULL |
| `target_type` | VARCHAR(32) | 饮食目标类型 | NULL |
| `avatar_url` | VARCHAR(512) | 头像地址 | NULL |
| `sort_no` | INT | 排序号 | NOT NULL, DEFAULT 0 |
| `status` | VARCHAR(32) | 成员状态 | NOT NULL |
| `created_at` | DATETIME | 创建时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | DATETIME | 更新时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE |
| `created_by` | VARCHAR(64) | 创建人 | NULL |
| `updated_by` | VARCHAR(64) | 更新人 | NULL |
| `deleted` | TINYINT(1) | 逻辑删除标记 | NOT NULL, DEFAULT 0 |

**索引**：
- `idx_family_member_family_id_deleted` - 家庭 ID 和删除标记索引
- `idx_family_member_family_id_status_deleted` - 家庭 ID、状态和删除标记索引
- `idx_family_member_family_id_role_type_deleted` - 家庭 ID、角色类型和删除标记索引

### member_preference - 成员偏好

**用途**：存储成员的口味偏好、忌口和营养目标

| 字段 | 类型 | 说明 | 约束 |
| --- | --- | --- | --- |
| `id` | BIGINT | 主键 | PK, AUTO_INCREMENT |
| `member_id` | BIGINT | 成员 ID | NOT NULL, UNIQUE |
| `taste_tags` | VARCHAR(512) | 口味标签（逗号分隔） | NULL |
| `avoid_ingredients` | VARCHAR(1024) | 忌口食材（逗号分隔） | NULL |
| `allergy_ingredients` | VARCHAR(1024) | 过敏食材（逗号分隔） | NULL |
| `spicy_level` | VARCHAR(32) | 辣度等级 | NULL |
| `sweet_level` | VARCHAR(32) | 甜度等级 | NULL |
| `oil_level` | VARCHAR(32) | 油量等级 | NULL |
| `salt_level` | VARCHAR(32) | 盐度等级 | NULL |
| `nutrition_goal_json` | JSON | 营养目标 JSON | NULL |
| `extra_rule_json` | JSON | 额外规则 JSON | NULL |
| `created_at` | DATETIME | 创建时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | DATETIME | 更新时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE |
| `created_by` | VARCHAR(64) | 创建人 | NULL |
| `updated_by` | VARCHAR(64) | 更新人 | NULL |

**索引**：
- `uk_member_preference_member_id` - 成员 ID 唯一索引
- `idx_member_preference_member_id` - 成员 ID 索引

## Recipe Context

### recipe - 菜品

**用途**：存储菜品基础信息、分类和适配标签

| 字段 | 类型 | 说明 | 约束 |
| --- | --- | --- | --- |
| `id` | BIGINT | 主键 | PK, AUTO_INCREMENT |
| `name` | VARCHAR(128) | 菜品名称 | NOT NULL, UNIQUE (with deleted) |
| `recipe_type` | VARCHAR(32) | 菜品类型 | NOT NULL |
| `source_type` | VARCHAR(32) | 来源类型 | NOT NULL |
| `season_tag` | VARCHAR(32) | 季节标签 | NULL |
| `crowd_tag` | VARCHAR(32) | 适配人群标签 | NULL |
| `taste_tag` | VARCHAR(512) | 口味标签（逗号分隔） | NULL |
| `difficulty_level` | VARCHAR(32) | 难度等级 | NULL |
| `cooking_time_min` | INT | 烹饪时长（分钟） | NULL |
| `cover_image_url` | VARCHAR(512) | 封面图地址 | NULL |
| `is_baby_friendly` | TINYINT(1) | 是否宝宝友好 | NOT NULL, DEFAULT 0 |
| `is_weight_loss_friendly` | TINYINT(1) | 是否减脂友好 | NOT NULL, DEFAULT 0 |
| `status` | VARCHAR(32) | 菜品状态 | NOT NULL |
| `created_at` | DATETIME | 创建时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | DATETIME | 更新时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE |
| `created_by` | VARCHAR(64) | 创建人 | NULL |
| `updated_by` | VARCHAR(64) | 更新人 | NULL |
| `deleted` | BIGINT | 逻辑删除标记 | NOT NULL, DEFAULT 0 |

**索引**：
- `uk_recipe_name_deleted` - 菜品名称和删除标记唯一索引
- `idx_recipe_type_deleted` - 菜品类型和删除标记索引
- `idx_recipe_season_crowd_deleted` - 季节、人群和删除标记索引
- `idx_recipe_status_deleted` - 状态和删除标记索引

**说明**：
- `deleted` 字段采用特殊设计：0 表示有效，删除时回填当前主键，确保软删除后名称可重用

### recipe_ingredient - 菜品食材

**用途**：存储菜品的食材明细

| 字段 | 类型 | 说明 | 约束 |
| --- | --- | --- | --- |
| `id` | BIGINT | 主键 | PK, AUTO_INCREMENT |
| `recipe_id` | BIGINT | 菜品 ID | NOT NULL |
| `ingredient_name` | VARCHAR(128) | 食材名称 | NOT NULL |
| `ingredient_type` | VARCHAR(32) | 食材类型 | NULL |
| `quantity` | DECIMAL(12, 2) | 数量 | NULL |
| `unit` | VARCHAR(32) | 单位 | NULL |
| `is_main` | TINYINT(1) | 是否主料 | NOT NULL, DEFAULT 0 |
| `sort_no` | INT | 排序号 | NOT NULL, DEFAULT 0 |
| `created_at` | DATETIME | 创建时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | DATETIME | 更新时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE |
| `created_by` | VARCHAR(64) | 创建人 | NULL |
| `updated_by` | VARCHAR(64) | 更新人 | NULL |

**索引**：
- `idx_recipe_ingredient_recipe_id` - 菜品 ID 索引
- `idx_recipe_ingredient_name` - 食材名称索引

### recipe_step - 菜品步骤

**用途**：存储菜品的烹饪步骤

| 字段 | 类型 | 说明 | 约束 |
| --- | --- | --- | --- |
| `id` | BIGINT | 主键 | PK, AUTO_INCREMENT |
| `recipe_id` | BIGINT | 菜品 ID | NOT NULL |
| `step_no` | INT | 步骤序号 | NOT NULL, UNIQUE (with recipe_id) |
| `content` | TEXT | 步骤内容 | NOT NULL |
| `image_url` | VARCHAR(512) | 步骤图片地址 | NULL |
| `created_at` | DATETIME | 创建时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | DATETIME | 更新时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE |
| `created_by` | VARCHAR(64) | 创建人 | NULL |
| `updated_by` | VARCHAR(64) | 更新人 | NULL |

**索引**：
- `uk_recipe_step_recipe_id_step_no` - 菜品 ID 和步骤序号唯一索引
- `idx_recipe_step_recipe_id` - 菜品 ID 索引

### recipe_nutrition - 菜品营养

**用途**：存储菜品的营养信息

| 字段 | 类型 | 说明 | 约束 |
| --- | --- | --- | --- |
| `id` | BIGINT | 主键 | PK, AUTO_INCREMENT |
| `recipe_id` | BIGINT | 菜品 ID | NOT NULL, UNIQUE |
| `calories` | DECIMAL(12, 2) | 热量 | NULL |
| `protein` | DECIMAL(12, 2) | 蛋白质 | NULL |
| `fat` | DECIMAL(12, 2) | 脂肪 | NULL |
| `carbohydrate` | DECIMAL(12, 2) | 碳水化合物 | NULL |
| `fiber` | DECIMAL(12, 2) | 膳食纤维 | NULL |
| `calcium` | DECIMAL(12, 2) | 钙 | NULL |
| `sodium` | DECIMAL(12, 2) | 钠 | NULL |
| `nutrition_json` | JSON | 营养扩展 JSON | NULL |
| `created_at` | DATETIME | 创建时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | DATETIME | 更新时间 | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE |
| `created_by` | VARCHAR(64) | 创建人 | NULL |
| `updated_by` | VARCHAR(64) | 更新人 | NULL |

**索引**：
- `uk_recipe_nutrition_recipe_id` - 菜品 ID 唯一索引

## 领域映射

### Family Context

- **FamilyProfile 聚合**：
  - 聚合根：`family_profile`
  - 子对象：`family_member`（通过 `family_id` 关联）
  - 子对象：`member_preference`（通过 `member_id` 关联）

### Recipe Context

- **Recipe 聚合**：
  - 聚合根：`recipe`
  - 子对象：`recipe_ingredient`（通过 `recipe_id` 关联）
  - 子对象：`recipe_step`（通过 `recipe_id` 关联）
  - 值对象：`recipe_nutrition`（通过 `recipe_id` 关联，1:1）

## 维护说明

- 本文件应在数据库结构变更后及时更新
- 更新方式：基于最新的 Flyway 迁移脚本重新生成
- 未来可考虑通过脚本自动生成此文档
