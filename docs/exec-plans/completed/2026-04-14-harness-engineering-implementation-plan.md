# Harness Engineering Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 为 MealMate 仓库建立一套面向 AI 理解与决策的文档骨架与知识导航体系。

**Architecture:** 通过新增 `ARCHITECTURE.md` 和 `docs/` 分层目录，补齐规格、设计、计划、参考资料与横切关注点入口；同时升级 `AGENTS.md` 和 `README.md`，让仓库形成一致的阅读顺序和维护约定。

**Tech Stack:** Markdown、Git、现有 COLA 5.0 Java 多模块工程

---

### Task 1: 建立设计与计划骨架

**Files:**
- Create: `docs/design-docs/index.md`
- Create: `docs/design-docs/core-beliefs.md`
- Create: `docs/design-docs/domain-context.md`
- Create: `docs/design-docs/delivery-scope.md`
- Create: `docs/design-docs/harness-engineering.md`
- Create: `docs/product-specs/index.md`
- Create: `docs/product-specs/new-user-onboarding.md`
- Create: `docs/exec-plans/active/README.md`
- Create: `docs/exec-plans/completed/README.md`
- Create: `docs/exec-plans/tech-debt-tracker.md`
- Create: `docs/generated/db-schema.md`
- Create: `docs/references/cola5-llms.txt`
- Create: `docs/references/spring-boot-llms.txt`
- Create: `docs/references/mybatis-plus-llms.txt`

**Step 1: 编写目录骨架文件**

为上述文件写入最小但可用的初始内容，确保每个目录都有明确职责、更新约定和链接入口。

**Step 2: 检查链接与命名**

确保目录命名与设计说明一致，避免混用 `business`、`plans` 与新目录时产生歧义。

### Task 2: 补齐横切关注点文档

**Files:**
- Create: `docs/DESIGN.md`
- Create: `docs/FRONTEND.md`
- Create: `docs/PLANS.md`
- Create: `docs/PRODUCT_SENSE.md`
- Create: `docs/QUALITY_SCORE.md`
- Create: `docs/RELIABILITY.md`
- Create: `docs/SECURITY.md`

**Step 1: 定义每份专题文档的作用**

每份文档应回答一个横切问题，例如质量如何评分、稳定性有哪些底线、安全有哪些禁区。

**Step 2: 保持内容面向 AI 判定**

文档需尽量使用可操作、可判断的表达，避免纯口号或纯概念描述。

### Task 3: 升级根入口文档

**Files:**
- Modify: `AGENTS.md`
- Create: `ARCHITECTURE.md`
- Modify: `README.md`

**Step 1: 升级 `AGENTS.md`**

补充文档阅读顺序、知识优先级、何时更新哪类文档、与新 `docs/` 体系的衔接。

**Step 2: 新建 `ARCHITECTURE.md`**

汇总工程事实、模块职责、依赖方向、对象流转与关键业务上下文，避免 AI 只能从 README 和零散文档拼装事实。

**Step 3: 收口 `README.md`**

让 README 退回总导航角色，链接到新体系，减少它与新文档之间的职责重叠。

### Task 4: 验证结构与可读性

**Files:**
- Verify only

**Step 1: 列出新目录**

运行目录检查命令，确认所有目标文件与目录已创建。

**Step 2: 检查核心文档互链**

检查 `README.md`、`AGENTS.md`、`ARCHITECTURE.md`、`docs/design-docs/index.md`、`docs/product-specs/index.md` 是否互相可达。

**Step 3: 人工校验是否形成 system of record**

确认 AI 首次进入仓库时，能够判断：

- 事实去哪看。
- 规格去哪看。
- 计划去哪看。
- 横切约束去哪看。

Plan complete and archived at `docs/exec-plans/completed/2026-04-14-harness-engineering-implementation-plan.md`. Two execution options:

**1. Subagent-Driven (this session)** - I dispatch fresh subagent per task, review between tasks, fast iteration

**2. Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints

**Which approach?**
