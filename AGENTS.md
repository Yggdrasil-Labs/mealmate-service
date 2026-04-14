# MealMate Agent Guide

本文件是 MealMate 仓库的导航页，主要服务 AI Agent、自动化流程和新进入仓库的协作者。

它回答三件事：

1. 第一次进入仓库时先读什么。
2. 不同类型的问题应该去哪类文档找答案。
3. 改动发生后应该更新哪类文档。

具体的技术栈、模块边界、依赖方向和对象流转，请统一查看 [ARCHITECTURE.md](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/ARCHITECTURE.md)。

## 我现在该读什么

- 如果你要理解技术栈、模块分层、依赖方向和技术边界：先读 `ARCHITECTURE.md`
- 如果你要确认业务术语、范围和产品边界：先读 `docs/PRODUCT_SENSE.md`
- 如果你要找设计原则、领域上下文和长期设计决策：先读 `docs/design-docs/index.md`
- 如果你要找计划、提案或历史记录：先读 `docs/PLANS.md`
- 如果你只知道“去 docs 里找”，但不知道先看哪篇：先读 `docs/index.md`

## 建议阅读顺序

首次进入仓库时，建议按以下顺序建立上下文：

1. `AGENTS.md`
2. `docs/index.md`
3. `ARCHITECTURE.md`
4. `docs/PRODUCT_SENSE.md`
5. `docs/PLANS.md`
6. `docs/design-docs/index.md`
7. `docs/product-specs/index.md`
8. `README.md`

## 2. 文档分工

### `ARCHITECTURE.md`

回答：

- 当前技术栈是什么
- 模块如何分层
- 技术边界与依赖方向是什么
- 对象和数据如何流转

### `docs/product-specs/`

回答：

- 要做什么
- 目标用户是谁
- 范围与验收标准是什么

### `docs/design-docs/`

回答：

- 为什么这样设计
- 领域上下文是什么
- 当前阶段有哪些设计边界和核心信念

### `docs/exec-plans/`

回答：

- 当前准备怎么推进
- 哪些计划在进行中
- 哪些计划已完成归档

### `docs/references/`

回答：

- 有哪些可重复查阅的外部知识摘要

### `docs/generated/`

回答：

- 哪些内容属于机器生成的事实快照

## 3. 文档优先级

当多个文档同时涉及同一主题时，默认按以下优先级理解：

1. 用户当前明确指令
2. `AGENTS.md`
3. `ARCHITECTURE.md`
4. `docs/product-specs/*`
5. `docs/design-docs/*`
6. `docs/exec-plans/*`
7. `docs/references/*`
8. `README.md`

## 4. 更新规则

- 需求目标、验收范围变化：更新 `docs/product-specs/*`
- 长期设计原则、领域边界变化：更新 `docs/design-docs/*`
- 技术栈、模块边界、依赖方向变化：更新 `ARCHITECTURE.md`
- 单次实施步骤、任务分解、执行状态变化：更新 `docs/exec-plans/*`
- 新增外部知识摘要：更新 `docs/references/*`
- 机器生成的事实快照变化：更新 `docs/generated/*`

## 5. 协作约束

- 默认使用中文撰写面向 AI 的仓库文档。
- 仓库中的每类知识应尽量只有一个主入口，避免并行真相源。
- 新结论优先沉淀到仓库文档，而不是只留在对话中。
- 技术实现必须遵守 [ARCHITECTURE.md](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/ARCHITECTURE.md) 中定义的分层与边界。

## 文档维护规则

- `AGENTS.md` 保持短小，只做导航与事实入口。
- 技术栈、模块边界、依赖方向变化时，同步更新 `ARCHITECTURE.md`。
- 业务语义与产品边界变化时，同步更新 `docs/PRODUCT_SENSE.md`。
- 新的长期设计决策放到 `docs/design-docs/`，不要混入一次性计划。
- 执行计划与阶段性工作记录放到 `docs/exec-plans/`。

## 不要这样做

- 不要把这个文件重新写成长篇规则大全。
- 不要把仓库未来可能存在的系统写成当前事实。
- 不要让同一条规范同时出现在多个入口页里且语义不一致。

## 下一跳

- 仓库结构与技术边界：[ARCHITECTURE.md](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/ARCHITECTURE.md)
- `docs/` 总导航：[docs/index.md](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/docs/index.md)
