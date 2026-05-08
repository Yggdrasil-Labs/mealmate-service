---
updated: 2026-05-08
---

# Business Domains

## Overview

MealMate 的核心不是"菜谱展示"，而是围绕家庭饮食决策构建的家庭饮食规划与执行系统。系统要同时覆盖家庭成员差异化饮食约束、计划生成、执行准备、实际记录与营养复盘，形成可持续优化的业务闭环。

## Business Loop

1. 建立家庭成员画像与饮食约束
2. 管理菜品知识库与标签体系
3. 生成一周用餐计划
4. 生成采购清单与备菜计划
5. 记录实际饮食情况
6. 对比计划与实际，输出营养分析与优化建议

## Domain Contexts

| Context | Responsibility | Type |
| --- | --- | --- |
| Family Context | 家庭成员、偏好、忌口、权限、画像配置 | Core |
| Recipe Context | 菜品、配方、标签、营养信息、分类 | Core |
| MealPlan Context | 周计划、餐次安排、生成规则、重复校验 | Core |
| Preparation Context | 采购清单、备菜计划、保存方式 | Supporting |
| Record Context | 照片记录、备注、计划关联、差异比对 | Supporting |
| Nutrition Context | 营养报告、达标校验、建议输出 | Supporting |
| Recommendation Context | 季节推荐、应急食材、野菜活动建议 | Generic |
| Notification Context | 提醒配置、定时任务、消息投递 | Generic |

## Key Aggregates

| Aggregate | Responsibility | Key Rules |
| --- | --- | --- |
| `FamilyProfile` | 管理家庭整体饮食规则与成员画像 | 宝宝必须绑定幼儿饮食约束；仅有权限角色可改计划 |
| `Recipe` | 沉淀标准菜品能力 | 菜品必须具备类型与适配人群；宝宝菜和减脂菜要有专属约束字段 |
| `WeeklyMealPlan` | 管理一周饮食计划与调整 | 一周默认不重复；减脂餐按周目标均匀分布；宝宝餐必须通过适配校验 |
| `PrepPlan` | 派生备菜与采购执行方案 | 相同食材归并统计；宝宝专属与成人辛辣食材分组展示 |
| `MealRecord` | 记录实际食用情况 | 每条记录必须绑定日期与餐次；允许计划内关联与独立补录 |
| `NutritionReport` | 输出周度分析结果 | 分析结论必须能追溯到计划或实际记录数据 |

## Ubiquitous Language

- **家庭成员**：系统中的饮食参与者，如妻子、丈夫、宝宝
- **饮食画像**：成员口味偏好、忌口、饮食目标和限制条件的集合
- **菜品**：可被计划、推荐、记录的标准业务对象
- **餐次**：早餐、午餐、晚餐
- **周计划**：覆盖 7 天三餐的结构化用餐安排
- **减脂餐**：满足低脂、低卡、高纤维规则的特定菜品安排
- **宝宝适配**：菜品满足软烂、少盐少糖、无辛辣等幼儿约束
- **备菜计划**：面向执行层的食材预处理、保存和烹饪说明
- **采购清单**：统一使用 `ShoppingList`，禁用 `PurchaseList`
- **实际饮食记录**：用户真实食用情况的照片与备注记录
- **营养分析**：对饮食结构进行规则化评估的结果
- **提醒任务**：统一使用 `NotifyTask`，禁用 `Reminder`、`Task`

## Domain Services

- `WeekPlanGenerateDomainService` - 周计划生成
- `RecipeMatchDomainService` - 菜品匹配
- `DuplicateCheckDomainService` - 重复校验
- `NutritionEvaluateDomainService` - 营养评估
- `PrepPlanGenerateDomainService` - 备菜计划生成
- `SeasonalRecommendDomainService` - 季节推荐
- `WildVegetableGuideDomainService` - 野菜指南

## Domain Events

- `WeeklyMealPlanGenerated` - 周计划已生成
- `WeeklyMealPlanAdjusted` - 周计划已调整
- `ManualRecipeAdded` - 手动添加菜品
- `PrepPlanGenerated` - 备菜计划已生成
- `MealRecordCreated` - 饮食记录已创建
- `WeeklyNutritionReportGenerated` - 周营养报告已生成
- `ReminderTriggered` - 提醒已触发

## Layer Mapping

- **adapter**: 协议输入输出与参数校验，不表达业务规则
- **app**: 用例编排、事务边界和命令查询执行
- **domain**: 聚合、领域服务、统一语言和核心规则
- **infrastructure**: 仓储、网关、持久化与任务调度，不反向污染领域模型
