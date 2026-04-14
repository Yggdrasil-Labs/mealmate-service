# MealMate Harness Engineering 设计说明

## 背景

在本次设计开始时，仓库具备以下文档基础：

- `AGENTS.md`：约束 AI Agent 的编码与分层边界。
- `README.md`：提供业务背景、启动方式和有限文档导航。
- 业务上下文与实施边界文档：后续已收口到 `docs/design-docs/`。
- 单次需求设计与实施计划：后续已归档到 `docs/exec-plans/completed/`。

这些内容已经能支撑局部开发，但还没有形成一个适合 AI 持续理解、检索和决策的知识系统。主要问题包括：

- 仓库内缺少“先读什么、何时更新什么”的统一导航。
- 业务事实、设计原则、产品规格、执行计划和参考资料尚未显式分层。
- AI 很难判断哪些文档属于长期事实，哪些只代表某次任务的临时计划。
- 现有文档结构更偏向“给人看”，尚未完全优化为“给 AI 做上下文压缩与决策”。

## 目标

为 MealMate 建立一套以中文为主、面向 AI 理解与决策的 harness engineering 文档体系，使仓库成为稳定的系统记录来源。

本次只交付文档与治理骨架，不在本轮引入 CI 校验、自动生成脚本或额外工程插件。

## 设计原则

### 1. 仓库是系统记录

- 长期有效的事实必须落入仓库。
- 口头约定、聊天结论、临时推断不应长期悬空在仓库外。
- 同一类事实只维护一个主入口，避免平行真相来源。

### 2. 文档按“用途”而不是“作者”分层

文档不是按谁写来分类，而是按 AI 需要做什么来分类：

- `ARCHITECTURE.md`：工程事实与分层边界。
- `product-specs/`：要做什么。
- `design-docs/`：为什么这样设计。
- `exec-plans/`：接下来怎么做。
- `references/`：外部资料摘要，不是内部事实。
- `generated/`：可机器生成的快照事实。

### 3. 优先面向 AI 可判定性

文档应帮助 AI 快速判断：

- 这是事实、规则、建议还是待办。
- 该文档是否比其他文档优先。
- 变更某类代码时应同步更新哪些文档。
- 某项需求是已有规格延伸，还是需要新增规格。

### 4. 中文优先

当前体系主要服务 AI 在本仓库中的理解与决策，因此统一以中文为主，必要时保留英文术语或目录名以兼容工程习惯。

### 5. 渐进演进

- 先建立骨架和入口，不一次性写满所有细节。
- 对已有文档以复用、迁移、收口为主，不盲目重写。
- 允许保留旧目录，但新体系必须成为默认入口。

## 目标结构

```text
AGENTS.md
ARCHITECTURE.md
docs/
├── design-docs/
│   ├── index.md
│   ├── core-beliefs.md
│   ├── domain-context.md
│   ├── delivery-scope.md
│   └── harness-engineering.md
├── exec-plans/
│   ├── active/
│   ├── completed/
│   └── tech-debt-tracker.md
├── generated/
│   └── db-schema.md
├── product-specs/
│   ├── index.md
│   └── new-user-onboarding.md
├── references/
│   ├── cola5-llms.txt
│   ├── spring-boot-llms.txt
│   └── mybatis-plus-llms.txt
├── DESIGN.md
├── FRONTEND.md
├── PLANS.md
├── PRODUCT_SENSE.md
├── QUALITY_SCORE.md
├── RELIABILITY.md
└── SECURITY.md
```

## 各入口职责

### `AGENTS.md`

从“编码约束说明”升级为“AI 工作协议”：

- 说明文档优先级。
- 说明不同任务应先读哪些文档。
- 说明哪些变化必须同步更新文档。
- 保留原有 COLA、命名、提交、验证约束。

### `ARCHITECTURE.md`

提供稳定的工程事实面：

- 模块职责与依赖方向。
- 关键对象流转。
- 核心业务上下文与领域映射。
- 技术基线与运行入口。

它不承载实施计划和临时想法，只承载当前认可的工程事实。

### `docs/design-docs/`

存放长期设计知识：

- `index.md`：设计文档索引与阅读顺序。
- `core-beliefs.md`：仓库在产品、架构、AI 协作上的核心信念。
- `domain-context.md`：由现有业务与领域上下文迁移收口。
- `delivery-scope.md`：由现有范围与实施约束迁移收口。
- `harness-engineering.md`：说明本仓库文档系统如何服务 AI。

### `docs/product-specs/`

聚焦用户价值和业务规格，不写技术实现。

- `index.md`：列出现有规格、状态、入口。
- `new-user-onboarding.md`：作为第一份样例规格，给后续 spec 提供写法模板。

### `docs/exec-plans/`

将执行计划变成长期治理资产：

- `active/`：当前仍在推进的计划。
- `completed/`：完成后的计划归档。
- `tech-debt-tracker.md`：记录不是当前需求，但值得后续治理的债务。

设计落地后，历史计划已归档到 `docs/exec-plans/completed/`，后续统一使用 `docs/exec-plans/`。

### `docs/references/`

收录 AI 可重复利用的外部资料摘要，不直接当作内部事实。适合放：

- COLA 5 约束摘要。
- Spring Boot 约定式开发要点。
- MyBatis-Plus 使用注意事项。

### `docs/generated/`

为未来自动生成文档预留目录。本轮先创建 `db-schema.md` 占位，说明生成来源与维护方式。

### `docs/*.md` 顶层专题

这些文档用于横切关注点：

- `DESIGN.md`：设计文档如何使用。
- `FRONTEND.md`：前后端协作接口与集成边界。
- `PLANS.md`：实施计划生命周期。
- `PRODUCT_SENSE.md`：AI 做取舍时应优先优化什么。
- `QUALITY_SCORE.md`：代码与交付质量评分标准。
- `RELIABILITY.md`：稳定性与容错基线。
- `SECURITY.md`：安全基线与禁忌。

## 迁移策略

### 最终收口结果

- 领域与范围内容已并入 `docs/design-docs/`
- 历史计划已归档到 `docs/exec-plans/completed/`

新体系现在直接作为默认入口，不再保留旧目录作为并行主入口。

### README 只做总导航

`README.md` 不再承担完整知识面，而是：

- 简述项目定位。
- 给出启动方式。
- 指向 `AGENTS.md`、`ARCHITECTURE.md` 与 `docs/` 主入口。

## 风险与取舍

### 风险

- 同时保留旧目录与新目录，短期内仍存在一定重复感。
- 若后续不把新文档作为默认入口，体系会退化成“多一层目录”。

### 取舍

- 本轮优先建立可用结构与阅读顺序，不追求一次性清理所有历史痕迹。
- 先让 AI 有稳定导航，再逐步收敛旧文档。

## 验收标准

完成后应满足：

1. AI 首次进入仓库时，能通过 `AGENTS.md` 找到完整阅读路径。
2. 能清楚区分事实、规格、设计、计划、参考资料。
3. `README.md`、`AGENTS.md`、`ARCHITECTURE.md` 与 `docs/` 之间链接一致。
4. 仓库中至少存在一份产品规格样例、一份设计索引、一个计划目录和一个生成目录占位。
5. 不破坏当前 Java/COLA 工程约束与现有业务文档。
