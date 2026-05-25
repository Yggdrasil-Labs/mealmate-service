# Plan 文档规格

plan.md 承接 design，回答"怎么拆、谁执行、什么顺序"。**假设执行者对代码库零上下文。**

## 核心原则

- **No Placeholders**：每步有实际代码，不允许"TODO"、"类似 Task N"
- **TDD**：先写失败测试 → 跑失败 → 写最小实现 → 跑通过 → 提交
- **DRY / YAGNI**
- **Frequent Commits**：每个 Task 完成后 commit

## 必填章节

1. **目标**（一句话）
2. **执行模式**（sequential / parallel / mixed）
3. **文件结构**（所有涉及文件 + 操作 + 职责）
4. **Task 列表**（每个 Task 含 Files + Step 序列）
5. **风险与阻塞**
6. **决策日志**
7. **完成标准**

## Task 格式

每个 Task 包含：
- **Files**：精确路径（Create / Modify:行范围 / Test）
- **Step 序列**（每 step 2-5 分钟）：
  - 写失败测试（完整代码）
  - 运行确认失败（命令 + 预期输出）
  - 写最小实现（完整代码）
  - 运行确认通过（命令 + 预期输出）
  - 提交（git add + commit 命令）
- **depends_on**（如有依赖）

## frontmatter

```yaml
id: plan-{slug}
status: not-started | in-progress | completed | blocked
owner: ""
tags: []
created: YYYY-MM-DD
updated: YYYY-MM-DD
```

## 文件结构设计原则

- 每个文件一个职责，优先小文件
- 一起变的文件放在一起
- 遵循现有代码库结构，不擅自重构
