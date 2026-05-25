---
name: project-workflow
description: "MUST be used for creating requirements, archiving versions, modifying architecture constraints, or any docs-driven iteration. Covers requirement lifecycle (spec → design → plan → execute → archive), unified scoring review (pass on ≥4.0), version release, rule promotion, and orphan management."
version: "1.0.0"
author: project
keywords: ["workflow", "requirements", "versioning"]
relations:
  complements: ["harness-docs", "brainstorming"]
  conflicts: ["writing-plans"]
---

# Project Workflow

项目级工作流规范。驱动需求全生命周期（创建 → 审查 → 实施 → 归档）和版本迭代。包含 10 条规则，分为文档规格（format/）和步骤约束（workflow/）两类。

## 覆盖优先级

本 skill 是**项目级工作流规范**，优先级高于用户级 skill。当以下用户级 skill 的流程规则与本 skill 冲突时，以本 skill 为准：

- `writing-plans` → 计划格式、任务字段、执行规则由本 skill 定义
- `brainstorming` → 创意发散后，需求产出格式必须对接本 skill 的 spec 模板
- 其他用户级 skill 涉及文档产出时，输出格式遵循本 skill 的 `templates/`

## When to Activate

- 收到新功能或变更请求（→ 创建需求）
- 修改 `ARCHITECTURE.md` / `core-beliefs.md` 等长期约束（→ 修改长期约束）
- 人类提供版本号要求归档（→ 版本归档）
- 同一违规出现 ≥ 3 次（→ 规则晋升）
- 发现无 owner 的模块或代码路径（→ 孤儿管理）

## When NOT to Activate

- 小任务（≤ 3 task，单模块，无契约变更）→ 直接实施，无需正式文档
- 不涉及文档迭代的纯代码工作
- Bootstrap 新项目文档体系（由 harness-docs skill 负责）
- 定期文档园艺（由 harness-docs skill 负责）

## Rule Categories

### format/（文档规格——文档该长什么样）

| Rule | 场景 |
|------|------|
| `format/spec` | 写 spec.md |
| `format/design` | 写 design.md |
| `format/plan` | 写 plan.md |
| `format/release` | 写 release.md |
| `format/review` | 审查任何文档（评分维度） |

### workflow/（步骤约束——流程怎么走）

| Rule | 场景 |
|------|------|
| `workflow/grill` | 需求对齐阶段 |
| `workflow/mapping` | 文档间映射检查 |
| `workflow/review` | 审查流程与确认 |
| `workflow/execution` | 实施阶段（TDD、subagent） |
| `workflow/archive` | 版本归档 |

## Scenarios

| 场景 | 加载规则 |
|------|---------|
| 需求对齐 | `workflow/grill` |
| 写 spec | `format/spec` + `workflow/mapping` |
| 写 design | `format/design` + `workflow/mapping` |
| 写 plan | `format/plan` |
| 审查文档 | `format/review` + `workflow/review` |
| 执行计划 | `workflow/execution` |
| 版本归档 | `format/release` + `workflow/archive` |

---

## Workflow

### Step 1: 需求对齐（Grill）

与用户对齐设计意图，逐一追问决策树每个分支直到共识。→ 读 `rules/workflow/grill.md`

### Step 2: 分级

> 以下阈值为默认值，可根据项目规模调整。

| 级别 | 判定条件 | 产出物 |
|------|----------|--------|
| 小任务 | ≤ 3 task，单模块，无契约变更 | 无正式文档，直接实施 |
| 中任务 | 4-8 task，或跨 2 模块，或涉及契约/状态机变更 | design.md + plan.md |
| 大任务 | > 8 task，跨 3+ 模块，新领域接入，依赖方向变更 | spec.md + design.md + plan.md |

如果判定为小任务 → 直接实施，流程结束。中/大任务继续。

### Step 3: 工作空间隔离

询问用户是否使用 git worktree：
- **是** → 创建 `feat/{slug}` worktree
- **否** → 创建 `feat/{slug}` 分支

### Step 4: 创建需求目录

从 `templates/requirement/` 复制对应模板到 `docs/active/{slug}/`，填充 frontmatter，注册到 `docs/active/index.md`。

### Step 5: 填写文档 + 审查

按序填写，每份文档写完后审查。→ 读对应 `rules/format/{doc}` + `rules/workflow/mapping` + `rules/workflow/review`

**中任务**：design → 审查 → plan → 审查
**大任务**：spec → 审查 → design → 审查 → plan → 审查

### Step 6: 执行

> **⛔ Checkpoint：** 呈现 plan 摘要，收到用户明确指令后才开始。

按 plan 中的 Task 实施。→ 读 `rules/workflow/execution.md`

完成后询问用户："继续下一个需求"（→ Step 1）或"版本开发完成"（→ Step 7）。

### Step 7: 版本归档

→ 读 `rules/workflow/archive.md` + `rules/format/release.md`

**初期生成**：第一个需求进入 `in-progress` 时即创建 release.md 草稿，后续持续追加。
**小任务/bugfix**：在 release.md "独立提交"表中登记。
**归档**：用户确认版本号后执行完整归档流程。

---

## 其他场景

### 修改长期约束

1. 确认是否长期变更（否则用 design doc）
2. 在 `docs/design-docs/` 创建架构 RFC（前缀 `arch-`）
3. 执行变更 → 更新链接和时间戳

### 规则晋升

同一违规 ≥ 3 次：识别 → 编码为 lint 规则 → 验证 → 接入 CI

### 孤儿管理

发现模块无 owner → 登记 `tech-debt-tracker.md`（Owner: ORPHAN）。30 天未指派 → 归档或删除。

---

## 回退规则

```
plan 审查发现 design 有问题 → 回到 design 修复
design 审查发现 spec 有问题 → 回到 spec 修复
spec 需要澄清 → 暂停，请求人类输入
```

## 文档与代码冲突

以代码现状为主。显式指出冲突，补齐文档，记录到 `tech-debt-tracker.md`。

## Resource Loading

- **SKILL.md（本文件）**：始终加载，提供步骤骨架和索引
- **rules/format/*.md**：写文档时加载对应规格
- **rules/workflow/*.md**：执行流程步骤时加载对应约束
- **templates/**：创建文档时复制
