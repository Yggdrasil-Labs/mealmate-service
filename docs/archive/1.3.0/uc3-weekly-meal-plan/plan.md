---
id: plan-uc3-weekly-meal-plan
status: done
owner: ""
tags: [mealplan, core]
created: 2026-05-26
updated: 2026-05-26
---

# 计划：UC3 生成周计划（后端）

> **执行方式：** 使用 subagent 分派，串行执行有依赖 Task。

**目标：** 实现周计划生成、手动调整、确认及派生备菜计划与采购清单的完整后端链路。
**执行模式：** sequential（Task 间有依赖链）

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `mealmate-start/src/main/resources/db/migration/V5__create_meal_plan_tables.sql` | 新增 | 5 张新表 DDL |
| `mealmate-domain/.../mealplan/model/*.java` | 新增 | 领域模型 + 枚举 |
| `mealmate-domain/.../mealplan/repo/*.java` | 新增 | 仓储接口 |
| `mealmate-domain/.../mealplan/service/*.java` | 新增 | 领域服务（生成/过滤/去重/派生） |
| `mealmate-app/.../mealplan/**/*.java` | 新增 | AppService、Executor、DTO |
| `mealmate-adapter/.../web/mealplan/**/*.java` | 新增 | Controller、Web DTO、Convertor |
| `mealmate-infrastructure/.../mealplan/**/*.java` | 新增 | DO、Convertor、Repository 实现 |

---

## Task 1: 数据库迁移脚本

depends_on: []

**Files:**
- Create: `mealmate-start/src/main/resources/db/migration/V5__create_meal_plan_tables.sql`

- [x] **Step 1: 编写迁移脚本**

包含 5 张表：`weekly_meal_plan`、`meal_plan_item`、`prep_plan`、`prep_plan_item`、`shopping_item`。字段和索引按 design.md 数据模型定义。

- [x] **Step 2: 验证迁移**

Run: `./mvnw spring-boot:run -pl mealmate-start -am` 启动验证表创建成功。

- [x] **Step 3: 更新 db-schema.md**

同步 `docs/generated/db-schema.md`。

---

## Task 2: 领域模型与枚举

depends_on: []

**Files:**
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/enums/PlanStatus.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/enums/PlanSource.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/enums/MealType.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/enums/MealPlanCrowdType.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/enums/PrepTaskStatus.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/enums/PrepPriority.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/enums/PushStatus.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/WeeklyMealPlan.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/MealPlanItem.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/PrepPlan.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/PrepPlanItem.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/model/ShoppingItem.java`

- [x] **Step 1: 创建枚举类**

PlanStatus(DRAFT, CONFIRMED, ARCHIVED)、PlanSource(MANUAL, AI_GENERATED)、MealType(BREAKFAST, LUNCH, DINNER)、MealPlanCrowdType(FAMILY, WIFE, HUSBAND, BABY, WIFE_WEIGHT_LOSS)、PrepTaskStatus(TODO, DONE)、PrepPriority(HIGH, NORMAL, LOW)、PushStatus(INIT, SENT, FAILED)。

- [x] **Step 2: 创建领域实体**

WeeklyMealPlan（聚合根）、MealPlanItem、PrepPlan、PrepPlanItem、ShoppingItem，按 design 字段定义。

- [x] **Step 3: 验证编译**

Run: `./mvnw compile -pl mealmate-domain -am`

---

## Task 3: 仓储接口

depends_on: [Task 2]

**Files:**
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/repo/WeeklyMealPlanRepository.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/repo/PrepPlanRepository.java`

- [x] **Step 1: 定义接口**

WeeklyMealPlanRepository：save, findByFamilyIdAndWeekStartDate, findById, findByIdWithItems, updateStatus, deleteItemsByPlanId, saveShoppingItems。
PrepPlanRepository：save, findByPlanId, updateItemStatus。

- [x] **Step 2: 验证编译**

Run: `./mvnw compile -pl mealmate-domain -am`

---

## Task 4: 领域服务 + 单元测试

depends_on: [Task 2]

**Files:**
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/service/IngredientFilterDomainService.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/service/DuplicateCheckDomainService.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/service/WeekPlanGenerateDomainService.java`
- Create: `mealmate-domain/src/main/java/io/yggdrasil/labs/mealmate/domain/mealplan/service/PrepPlanDeriveDomainService.java`
- Test: `mealmate-domain/src/test/java/io/yggdrasil/labs/mealmate/domain/mealplan/service/*Test.java`

- [x] **Step 1: IngredientFilterDomainService — 测试 + 实现**

测试：忌口食材过滤、过敏食材过滤、无忌口时全部通过。

- [x] **Step 2: DuplicateCheckDomainService — 测试 + 实现**

测试：同菜品重复标记、不同菜品全 false。

- [x] **Step 3: WeekPlanGenerateDomainService — 测试 + 实现**

测试：正常 7×3 生成、候选不足降级、每天至少 1 减脂餐、每天至少 2 宝宝餐、早餐优先短时长。
实现：规则链 R-05→R-04→R-01→R-02→R-03→R-06→R-07 + 降级循环。

- [x] **Step 4: PrepPlanDeriveDomainService — 测试 + 实现**

测试：食材归并、保存方式标注、采购清单去重。

- [x] **Step 5: 运行全部领域测试**

Run: `./mvnw test -pl mealmate-domain -am -Dtest="io.yggdrasil.labs.mealmate.domain.mealplan.**" -Dsurefire.failIfNoSpecifiedTests=false`

---

## Task 5: Infrastructure 层

depends_on: [Task 1, Task 3]

**Files:**
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/mealplan/dataobject/*.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/mealplan/convertor/*.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/impl/WeeklyMealPlanRepositoryImpl.java`
- Create: `mealmate-infrastructure/src/main/java/io/yggdrasil/labs/mealmate/infrastructure/persistence/impl/PrepPlanRepositoryImpl.java`

- [x] **Step 1: 创建 DO 类（5 个）**

使用 `@AutoMybatis` 注解，字段与 DDL 对应。

- [x] **Step 2: 创建 InfraConvertor（MapStruct）**

Entity ↔ DO 互转。

- [x] **Step 3: 实现 WeeklyMealPlanRepositoryImpl**

覆盖 DRAFT 逻辑：SELECT FOR UPDATE + 逻辑删除旧计划 + 物理删除旧 items + 插入新计划。

- [x] **Step 4: 实现 PrepPlanRepositoryImpl**

- [x] **Step 5: 验证编译**

Run: `./mvnw compile -pl mealmate-infrastructure -am`

---

## Task 6: App 层

depends_on: [Task 4, Task 5]

**Files:**
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/mealplan/dto/cmd/*.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/mealplan/dto/qry/*.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/mealplan/dto/co/*.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/mealplan/convertor/MealPlanConvertor.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/mealplan/assembler/MealPlanAssembler.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/mealplan/executor/*.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/mealplan/application/MealPlanAppService.java`
- Create: `mealmate-app/src/main/java/io/yggdrasil/labs/mealmate/app/mealplan/application/MealPlanAppConfiguration.java`

- [x] **Step 1: 创建 DTO（Cmd/Qry/CO）**

- [x] **Step 2: 创建 Convertor + Assembler（MapStruct）**

- [x] **Step 3: 实现 12 个 Executor**

GenerateWeeklyPlanCmdExe、ReplaceItemCmdExe、AddItemCmdExe、DeleteItemCmdExe、ManualAddItemCmdExe、ConfirmMealPlanCmdExe、GetCurrentWeekPlanQryExe、GetMealPlanDetailQryExe、GetPrepPlanQryExe、UpdatePrepItemStatusCmdExe、GetShoppingListQryExe、UpdateShoppingItemCmdExe。

- [x] **Step 4: 实现 MealPlanAppService + Configuration**

- [x] **Step 5: 验证编译**

Run: `./mvnw compile -pl mealmate-app -am`

---

## Task 7: Adapter 层

depends_on: [Task 6]

**Files:**
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/mealplan/MealPlanController.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/mealplan/dto/*.java`
- Create: `mealmate-adapter/src/main/java/io/yggdrasil/labs/mealmate/adapter/web/mealplan/convertor/MealPlanWebConvertor.java`

- [x] **Step 1: 创建 Web Request/Response DTO**

- [x] **Step 2: 创建 MealPlanWebConvertor（MapStruct）**

- [x] **Step 3: 实现 MealPlanController（12 端点）**

- [x] **Step 4: 验证编译**

Run: `./mvnw compile -pl mealmate-adapter -am`

---

## Task 8: 后端集成验证

depends_on: [Task 7]

- [x] **Step 1: 全量构建**

Run: `./mvnw clean verify`

- [x] **Step 2: Spotless 格式化**

Run: `./mvnw spotless:apply`

- [x] **Step 3: 启动验证**

Run: `./mvnw spring-boot:run -pl mealmate-start -am`
验证 Swagger UI 展示所有 meal-plan 端点。

---

## 风险与阻塞

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 生成算法在候选极少时正确性 | 中 | 单测覆盖极端场景，降级策略保证不抛异常 |
| 唯一约束并发冲突 | 低 | SELECT FOR UPDATE + 事务重试 |
| MapStruct 生成代码冲突 | 低 | 统一 componentModel = "spring" |

## 决策日志

<!-- 执行过程中记录 -->

## 完成标准

- [x] 所有 Task checkbox 勾选
- [x] `./mvnw clean verify` 通过
- [x] 12 个 API 端点可通过 Swagger 调用
- [x] 领域服务单测全部通过
- [x] design status 更新为 verified
- [x] 剩余债务记录到 `tech-debt-tracker.md`
