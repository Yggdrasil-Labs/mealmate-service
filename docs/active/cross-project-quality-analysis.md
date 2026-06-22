# MealMate 项目代码质量深度分析报告

日期：2026-06-22

## 报告定位

本报告是对 MealMate 全栈项目（mealmate-service + mealmate-web + mealmate-e2e）的跨子项目系统性问题分析。各子项目内部问题见：
- 后端：`mealmate-service/docs/active/code-quality-analysis.md`
- 前端：`mealmate-web/docs/active/code-quality-analysis.md`

---

## 跨项目系统性问题

### 1. 前后端错误链路完全断裂（P1）

**问题链**：
```
后端 AppService 抛出 IllegalArgumentException("PLAN_NOT_FOUND")
  → 无 @ControllerAdvice 捕获
  → Spring Boot 返回 HTTP 500 + 默认 JSON（无 errCode）
  → 前端 request.ts 收到 500，读 data.errMessage → undefined
  → Fallback 到 "服务器内部错误"
  → ElMessage.error 被注释 → 用户无任何提示
  → Store try/finally 吞掉错误 → 页面只是 loading 消失
```

**影响**：后端精心设计的 10+ 业务错误码（`PLAN_NOT_FOUND`、`MEAL_PLAN_ALREADY_CONFIRMED`、`MEAL_PLAN_ITEM_LAST_ONE`、`CANDIDATE_RECIPES_INSUFFICIENT` 等）全部无法到达用户。

**修复顺序**：
1. 后端新增 `GlobalExceptionHandler`（2h）
2. 前端取消 `ElMessage.error` 注释（0.5h）
3. 前端 Store/Composable 补 error state（4h）
4. 前端特定错误码处理（按需）

### 2. 前后端字段命名不一致（P1）

**根因**：无统一契约层（如 OpenAPI spec → code-gen）。

**具体案例**：
- 后端 `MealPlanItemCO.isWeightLoss`（boolean 原始类型）→ Jackson 序列化为 `"weightLoss"`
- 前端 `types.ts` 定义为 `isWeightLoss` → 运行时永远读到 `undefined`

**结果**："宝宝餐"和"减脂餐"标签在对接真实后端时永远不显示。

**修复方案**：
- 短期：前端 types.ts 改为 `weightLoss`/`babyMeal`（与 Jackson 输出对齐）
- 中期：后端启用 springdoc OpenAPI 生成 spec → 前端基于 spec 生成类型

### 3. 认证/鉴权完全空白（P0 — 上线阻塞）

三个子项目一致标注"TODO: 接入认证"：
- 后端：无 Spring Security、无 token 验证、IDOR 越权
- 前端：`request.ts` 有 401/403 处理分支但后端永不触发
- E2E：`harness/setup/` 是预建骨架

**建议**：独立规划认证体系（JWT + familyId 归属校验），不混入日常 sprint。

### 4. 缺少 CI/CD 自动化

- 三个子项目都有 `.github/` 目录
- 未见实际 workflow 文件调度 lint/test/build
- 代码质量门禁完全依赖开发者手动运行

---

## 修复优先级建议

### Sprint 1（阻断风险 — 1-2 天）

| 序号 | 问题 | 子项目 | 工作量 |
|------|------|--------|--------|
| 1 | N+1 查询修复 | service | 1-2h |
| 2 | 全局异常处理器 | service | 2h |
| 3 | 前端 ElMessage 取消注释 + types.ts 字段修正 | web | 1.5h |
| 4 | navigateWeek 提取 composable | web | 0.5h |

### Sprint 2（功能完善 — 3-4 天）

| 序号 | 问题 | 子项目 | 工作量 |
|------|------|--------|--------|
| 5 | Store/Composable 补 error state | web | 4h |
| 6 | MealPlanAppService 补应用层测试 | service | 4h |
| 7 | KeepAlive 依赖移动 + 双路由清理 + resetStores | web | 1.5h |
| 8 | 404 路由 + ErrorBoundary | web | 1h |

### Sprint 3（架构优化）

| 序号 | 问题 | 子项目 | 工作量 |
|------|------|--------|--------|
| 9 | MealPlanAppService 拆 Executor | service | 8h |
| 10 | WeeklyMealPlan 聚合根重构 | service | 8h |

### 独立规划

| 问题 | 涉及子项目 | 说明 |
|------|-----------|------|
| 认证体系 | service + web + e2e | 需 spec → design → plan 完整流程 |
| OpenAPI 契约对齐 | service + web | 中期，消除命名漂移 |
| CI/CD 流水线 | 全部 | 部署前必须完成 |

---

## 审查质量评估

- 原始审查 12 条结论中：8 条准确成立、2 条需修正方向/程度、2 条被证伪
- 整体命中率 67% 完全正确
- 证伪原因：搜索不彻底（漏查已有测试文件）、未读 Configuration 注释就下结论
- 所有保留结论均经过逐文件代码验证和行为链路分析
