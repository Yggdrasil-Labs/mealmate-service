# docs 导航

如果你已经进入 `docs/`，但不确定下一步看什么，从这里开始。

## 推荐阅读路径

如果你是第一次进入仓库，建议按以下顺序阅读：

1. [AGENTS.md](../AGENTS.md) - AI 工作协议与导航
2. [ARCHITECTURE.md](../ARCHITECTURE.md) - 工程事实与技术边界
3. [PRODUCT_SENSE.md](./PRODUCT_SENSE.md) - 产品目标与取舍原则
4. [design-docs/index.md](./design-docs/index.md) - 设计文档索引
5. [product-specs/index.md](./product-specs/index.md) - 产品规格索引
6. [PLANS.md](./PLANS.md) - 执行计划说明

## 按需查阅

- **想理解产品目标、业务取舍和统一语言**：看 [PRODUCT_SENSE.md](./PRODUCT_SENSE.md)
- **想理解设计原则、领域上下文和长期决策**：看 [design-docs/index.md](./design-docs/index.md)
- **想理解执行计划和历史归档**：看 [PLANS.md](./PLANS.md)
- **想看横切约束**：看 [QUALITY_SCORE.md](./QUALITY_SCORE.md)、[RELIABILITY.md](./RELIABILITY.md)、[SECURITY.md](./SECURITY.md)
- **想看前后端协作边界**：看 [FRONTEND.md](./FRONTEND.md)
- **想看外部知识摘要**：看 [references/](./references/)
- **想看机器生成内容**：看 [generated/](./generated/)

## 目录分工

### `design-docs/`
长期设计知识、领域上下文、设计信念和决策。

**何时查看**：需要理解"为什么这样设计"时。

**主要文档**：
- [core-beliefs.md](./design-docs/core-beliefs.md) - 核心信念
- [domain-context.md](./design-docs/domain-context.md) - 领域上下文
- [delivery-scope.md](./design-docs/delivery-scope.md) - 交付范围
- [harness-engineering.md](./design-docs/harness-engineering.md) - 文档体系

### `product-specs/`
产品规格、业务目标、范围和验收标准。

**何时查看**：需要理解"要做什么"时。

**主要文档**：
- [new-user-onboarding.md](./product-specs/new-user-onboarding.md) - 新用户接入

### `exec-plans/`
进行中的计划、已归档计划和技术债跟踪。

**何时查看**：需要理解"怎么分步推进"时。

**主要目录**：
- [active/](./exec-plans/active/) - 进行中的计划
- [completed/](./exec-plans/completed/) - 已完成的计划
- [tech-debt-tracker.md](./exec-plans/tech-debt-tracker.md) - 技术债追踪

### `references/`
可重复查阅的外部资料摘要。

**何时查看**：需要快速查阅技术栈约定时。

**主要文档**：
- [cola5-llms.txt](./references/cola5-llms.txt) - COLA 5.0 约定
- [spring-boot-llms.txt](./references/spring-boot-llms.txt) - Spring Boot 约定
- [mybatis-plus-llms.txt](./references/mybatis-plus-llms.txt) - MyBatis-Plus 约定

### `generated/`
机器生成的事实快照。

**何时查看**：需要查看数据库结构等生成内容时。

**主要文档**：
- [db-schema.md](./generated/db-schema.md) - 数据库结构快照

## 横切关注点文档

这些文档提供跨领域的约束和指导：

- [DESIGN.md](./DESIGN.md) - 设计文档使用说明
- [PLANS.md](./PLANS.md) - 执行计划说明
- [PRODUCT_SENSE.md](./PRODUCT_SENSE.md) - 产品感知
- [QUALITY_SCORE.md](./QUALITY_SCORE.md) - 质量评分标准
- [RELIABILITY.md](./RELIABILITY.md) - 稳定性基线
- [SECURITY.md](./SECURITY.md) - 安全基线
- [FRONTEND.md](./FRONTEND.md) - 前后端协作边界

## 文档更新规则

- **需求目标、验收范围变化**：更新 `product-specs/*`
- **长期设计原则、领域边界变化**：更新 `design-docs/*`
- **技术栈、模块边界、依赖方向变化**：更新 `ARCHITECTURE.md`
- **单次实施步骤、任务分解、执行状态变化**：更新 `exec-plans/*`
- **新增外部知识摘要**：更新 `references/*`
- **机器生成的事实快照变化**：更新 `generated/*`
