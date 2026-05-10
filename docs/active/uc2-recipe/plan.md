---
id: plan-uc2-recipe
status: completed
owner: "—"
tags: [recipe, backend]
created: 2026-05-08
updated: 2026-05-10
---

> 本文档承接 design.md，回答"怎么拆、谁执行、什么顺序"。同目录下的 spec.md 和 design.md 共享相同 slug。

# 计划：菜品库管理（uc2-recipe）

## 目标

完成 UC2 菜品库后端能力，覆盖数据库迁移、领域规则、仓储持久化、应用编排、HTTP API 和自动化测试，使菜品可以作为后续周计划、备菜采购和营养复盘的结构化输入。

## 执行模式

模式：sequential

## 任务列表

### T1: 数据库迁移
- depends_on: []
- scope: `mealmate-start/src/main/resources/db/migration/V4__create_recipe_tables.sql`
- verify: `./mvnw -pl mealmate-start -am test -Dtest=FlywayMigrationSmokeTest -Dsurefire.failIfNoSpecifiedTests=false`
- agent: main
- status: done
- deliverable: 创建 `recipe`、`recipe_ingredient`、`recipe_step`、`recipe_nutrition` 四张表

### T2: 领域模型与领域规则
- depends_on: [T1]
- scope: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/recipe/`
- verify: `./mvnw -pl mealmate-domain test -Dtest=RecipeDomainServiceTest`
- agent: main
- status: done
- deliverable: `Recipe` 聚合、子对象、枚举、仓储接口和 `RecipeDomainService`

### T3: 基础设施持久化
- depends_on: [T2]
- scope: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/recipe/` 与 `RecipeRepositoryImpl`
- verify: `./mvnw -pl mealmate-infrastructure -am test -Dtest=RecipeInfraConvertorTest,RecipeRepositoryImplTest -Dsurefire.failIfNoSpecifiedTests=false`
- agent: main
- status: done
- deliverable: DO、InfraConvertor、聚合装配、分页筛选、保存、更新、删除实现

### T4: 应用层编排
- depends_on: [T2, T3]
- scope: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/recipe/`
- verify: `./mvnw -pl mealmate-app -am test -Dtest='*Recipe*Test' -Dsurefire.failIfNoSpecifiedTests=false`
- agent: main
- status: done
- deliverable: `RecipeAppService`、Cmd/Qry DTO、Assembler、Convertor、Executor

### T5: Web API 适配
- depends_on: [T4]
- scope: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/recipe/`
- verify: `./mvnw -pl mealmate-adapter -am test -Dtest=RecipeControllerTest -Dsurefire.failIfNoSpecifiedTests=false`
- agent: main
- status: done
- deliverable: `/api/recipes` REST API、Web DTO、Web Convertor 和 OpenAPI 注解

### T6: 端到端集成验证
- depends_on: [T1, T2, T3, T4, T5]
- scope: `mealmate-start/src/test/java/io/yggdrasil/labs/mealmate/start/CreateRecipeApiIntegrationTest.java`
- verify: `./mvnw -pl mealmate-start -am test -Dtest=CreateRecipeApiIntegrationTest -Dsurefire.failIfNoSpecifiedTests=false`
- agent: main
- status: done
- deliverable: 覆盖 HTTP 创建并持久化子表、分页筛选、删除后同名复用

### T7: 测试环境修复
- depends_on: [T4, T5, T6]
- scope: `mealmate-{app,adapter,infrastructure,start}/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker`
- verify: `./mvnw test -Dsurefire.failIfNoSpecifiedTests=false`
- agent: main
- status: done
- deliverable: 将 Mockito 配置为 `mock-maker-subclass`，避免 WSL/JDK 环境下 inline agent attach 失败

## 决策日志

- 2026-05-10 — 采用四表聚合持久化 — 菜品、食材、步骤和营养信息需要被后续计划、采购和营养复盘结构化消费。
- 2026-05-10 — 逻辑删除主表并清理子表 — 保留删除记录以支持同名复用，同时避免子表孤儿数据影响聚合查询。
- 2026-05-10 — 使用 `mock-maker-subclass` — 当前 WSL/JDK 环境无法稳定支持 Mockito inline mock maker 自附加。

## 风险与阻塞

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 默认 Surefire 命名规则不自动执行 `*IntegrationTest` | 全量 `./mvnw test` 不能证明 UC2 端到端场景 | 在本计划中显式记录 `CreateRecipeApiIntegrationTest` 的 verify 命令 |
| 删除策略与未来审计需求冲突 | 已删除子表无法直接恢复详情 | 如后续需要审计，新增归档表或事件日志，不改变当前查询语义 |
| 系统预置菜品导入尚未实现 | 只能维护手动菜品 | 保留 `source_type`，后续以独立需求接入系统菜品导入 |

## 完成标准

- [x] 所有任务 status = done
- [x] 测试通过（单元 + 集成）
- [x] design-doc status 更新为 verified
- [x] spec status 更新为 shipped
- [x] 剩余债务已检查；暂无 UC2 专属新增债务
