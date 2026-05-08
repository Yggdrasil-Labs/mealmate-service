# MealMate Agent Guide

MealMate 仓库导航页，服务 AI Agent 和协作者。

## 快速导航

| 问题 | 文档 |
|---|---|
| 技术栈、模块分层、依赖方向 | [ARCHITECTURE.md](./ARCHITECTURE.md) |
| 业务术语、产品边界 | [docs/PRODUCT_SENSE.md](./docs/PRODUCT_SENSE.md) |
| 设计原则、领域上下文 | [docs/design-docs/index.md](./docs/design-docs/index.md) |
| 活跃需求、技术债 | [docs/active/index.md](./docs/active/index.md) |
| 版本归档 | [docs/archive/index.md](./docs/archive/index.md) |
| 工作流程、审查方法 | [docs/guides/WORKFLOW.md](./docs/guides/WORKFLOW.md) |
| 外部知识摘要 | [docs/references/](./docs/references/) |
| 机器生成快照 | [docs/generated/](./docs/generated/) |

## 建议阅读顺序

1. `AGENTS.md` - 本文件
2. `ARCHITECTURE.md` - 工程事实与技术边界
3. `docs/PRODUCT_SENSE.md` - 产品目标与取舍原则
4. `docs/guides/WORKFLOW.md` - 需求工作流
5. `docs/design-docs/index.md` - 设计文档索引
6. `docs/active/index.md` - 活跃需求

## 文档更新规则

| 变化类型 | 更新位置 |
|---|---|
| 需求目标、验收标准 | `docs/active/{slug}/spec.md` |
| 技术设计、实现方案 | `docs/active/{slug}/design.md` |
| 执行计划、任务分解 | `docs/active/{slug}/plan.md` |
| 长期设计决策 | `docs/design-docs/` |
| 技术栈、模块边界 | `ARCHITECTURE.md` |
| 产品边界、取舍原则 | `docs/PRODUCT_SENSE.md` |
| 技术债 | `docs/active/tech-debt-tracker.md` |

## 开发命令

```bash
# 构建
./mvnw clean verify

# 启动
./mvnw spring-boot:run -pl mealmate-start -am

# 格式化
./mvnw spotless:apply

# 单测
./mvnw test -pl mealmate-domain -am
```

## 协作约束

- 仓库文档使用中文
- 每类知识只有一个主入口
- 新结论沉淀到文档，不只留在对话中
- 技术实现遵守 ARCHITECTURE.md 定义的分层与边界
- 新需求使用 `docs/active/{slug}/` 结构，包含 spec/design/plan
- 完成的需求归档到 `docs/archive/{version}/`

## 文档优先级

1. 用户当前明确指令
2. `AGENTS.md`
3. `ARCHITECTURE.md`
4. `docs/active/*`
5. `docs/design-docs/*`
6. `docs/references/*`
7. `README.md`
