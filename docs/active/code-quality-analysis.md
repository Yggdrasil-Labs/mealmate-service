# 后端代码质量深度分析报告

日期：2026-06-22

## 概述

本报告基于对 mealmate-service 全量源码的逐文件审查，聚焦已确认成立的架构和实现问题。每条结论附有代码路径、行为证据和修复方案。

---

## P0-1：零认证与 IDOR 越权

### 证据

- 全项目 pom.xml 无 `spring-boot-starter-security` 依赖
- 无 `@PreAuthorize`、`@Secured`、`SecurityContextHolder` 使用
- `MealPlanAppService.assertPlanOwnership()` 仅验证 plan 存在，不验证归属家庭：
  ```java
  // MealPlanAppService.java L389
  // TODO: 接入认证后恢复 familyId 归属校验，防止 IDOR 越权。
  private WeeklyMealPlan assertPlanOwnership(Long planId) {
      return weeklyMealPlanRepository.findById(planId)
          .orElseThrow(() -> new IllegalArgumentException("PLAN_NOT_FOUND"));
  }
  ```
- `familyId` 由前端 `@RequestParam(required = false)` 传入，无认证上下文校验
- `MealPlanController`（14 端点）、`RecipeController`（8 端点）、`FamilyMemberController`（6 端点）全部裸露

### 影响

- 任何人可通过遍历 `planId` 获取他人家庭的周计划、采购清单、备菜计划
- 任何人可修改/删除他人家庭的菜品和成员数据
- 部署到任何公网环境即为 Critical 安全漏洞

### 修复路径

1. 引入 `spring-boot-starter-security` + JWT Token 验证
2. 实现 `SecurityContextHolder` → 当前用户 familyId 映射
3. `assertPlanOwnership` 改为比对 familyId 归属
4. 所有跨家庭操作添加 familyId 校验

---

## P0-2：N+1 查询 + 无上限候选加载

### 证据

**N+1 查询** — `MealPlanAppService.java` L396-403：
```java
// P2: 逐个查询（N+1），待 RecipeRepository 添加 findByIds 批量方法后优化
return recipeIds.stream()
    .map(recipeRepository::findById)   // 每条 recipe 一次独立 SQL
    .filter(Optional::isPresent)
    .map(Optional::get)
    .collect(Collectors.toMap(Recipe::getId, Function.identity()));
```

`RecipeRepository` 接口确认无 `findByIds` 批量方法（grep 验证）。

**无上限候选加载** — `MealPlanAppService.java` L94：
```java
List<Recipe> candidates = recipeRepository.page(
    RecipeQueryCriteria.builder().pageNum(1).pageSize(500).build());
```

### 量化影响

- 一个周计划 35 items → 30-35 个不同 recipeId → **30-35 次独立 SQL**
- `getCurrentWeekPlan`（每次打开页面）、`getPlanDetail`、`generateWeeklyPlan` 均触发
- 候选加载一次查询 500 条聚合根（含 ingredients/steps/nutrition 子集合）

### 修复方案

1. `RecipeRepository` 新增 `List<Recipe> findByIds(List<Long> ids)`
2. `RecipeRepositoryImpl` 使用 MyBatis-Plus `listByIds()` 单次查询
3. 中期：生成算法改为加载轻量投影而非完整聚合根
4. 工作量预估：1-2h

---

## P1-1：MealPlanAppService 过重 + 缺应用层编排测试

### 证据

| 维度 | MealPlanAppService | RecipeAppService | FamilyMemberAppService |
|------|-------------------|-----------------|----------------------|
| 代码行数 | 344 行 | 61 行 | 46 行 |
| 公开方法 | 16 个 | 11 个 | 8 个 |
| 注入依赖 | 12 个 | 11 个 Executor | 8 个 Executor |
| 组织模式 | 混合（大部分内联，仅 3 个委派） | 纯委派 | 纯委派 |
| 对应测试 | **0 个** | 11 个 | 8+1+1 个 |

跨聚合耦合：直接注入 `FamilyMemberRepository` 和 `MemberPreferenceRepository`。

### 未被验证的关键场景

1. `generateWeeklyPlan` 旧 DRAFT 覆盖的事务回滚正确性
2. `confirmPlan` 同时操作 3 张表的一致性
3. `deleteItem` "最后一项不可删除"规则
4. 并发 `generateWeeklyPlan` 的悲观锁有效性
5. `assertPlanDraft` 在 5 个写操作中的状态守卫

### 修复方案

- Phase 1：补应用层测试覆盖 5 个核心场景（4h）
- Phase 2：拆为独立 Executor（`GenerateWeeklyPlanCmdExe`、`ConfirmPlanCmdExe`、`DeleteItemCmdExe` 等），与 Recipe/Family 保持一致（8h）
- Phase 3：消除跨聚合直接依赖

---

## P1-2：全局异常处理缺失

### 证据

- 全项目 0 个 `@ControllerAdvice` / `@ExceptionHandler`（grep 确认）
- `cola-component-catchlog-starter` 已引入但从未使用 `@CatchAndLog` 注解（从未生效）
- 无自定义 `server.error.*` 配置

### 异常链路

```
AppService 抛出 IllegalArgumentException("PLAN_NOT_FOUND")
  → 穿透 Controller（无人捕获）
  → Spring Boot BasicErrorController 接管
  → HTTP 500 + Spring 默认 JSON（无 errCode/errMessage）
  → 前端收到 500，尝试读 data.errMessage → undefined
  → Fallback 到 "服务器内部错误"
  → 前端注释了 ElMessage.error → 用户无感知
```

**结果：后端定义的业务错误码永远无法到达前端。**

### 修复方案

`mealmate-adapter` 新增 `GlobalExceptionHandler`：
- `IllegalArgumentException` → HTTP 400 + `Response.buildFailure(errCode, msg)`
- `IllegalStateException` → HTTP 409 + `Response.buildFailure(errCode, msg)`
- `ConstraintViolationException` → HTTP 400 + 字段级错误信息
- `Exception` → HTTP 500 + 通用错误（日志记录完整堆栈）

工作量预估：2h

---

## P2-1：WeeklyMealPlan 贫血模型

### 证据

`WeeklyMealPlan.java` — 纯数据容器，零行为方法（对比 `Recipe.java` 有 `normalize()`、`assertEditable()`、`assertDeletable()`）。

应在聚合根上的规则（当前散落在 AppService）：
- `assertDraft()` — 状态守卫
- `findItemsBySlot(date, mealType)` — 同一餐次查询
- `removeItem(itemId)` + "最后一项不可删除"
- `confirm()` — 状态转换

### 影响

业务规则难复用、难独立测试，新入口复制 AppService 逻辑风险。

### 修复方案

将核心不变式下沉到聚合根，AppService 调用聚合根行为方法。工作量预估：8h。

---

## 综合优先级

| 编号 | 问题 | 修复工作量 | 建议阶段 |
|------|------|-----------|---------|
| P0-1 | 零认证 + IDOR | 大 | 独立规划 |
| P0-2 | N+1 查询 | 1-2h | Sprint 1 |
| P1-1 | AppService 测试 | 4h | Sprint 1 |
| P1-2 | 全局异常处理 | 2h | Sprint 1 |
| P2-1 | 贫血模型重构 | 8h | Sprint 2+ |

---

## 审查方法论说明

### 自我质疑修正

原始审查中 2 条结论被证伪：
1. ~~"领域服务不是 Spring Bean"~~ — 实际是有意的 DDD 设计（通过 `@Configuration` 装配，保持领域层无框架依赖）
2. ~~"MealPlan 核心算法零测试"~~ — 实际 `MealPlanDomainServiceTest` 覆盖了 WeekPlanGenerateDomainService、IngredientFilterDomainService、DuplicateCheckDomainService、PrepPlanDeriveDomainService。准确表述为"应用层编排无测试"
