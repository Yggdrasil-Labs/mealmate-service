---
id: plan-{slug}
status: not-started
owner: ""
tags: []
created: YYYY-MM-DD
updated: YYYY-MM-DD
---

# 计划：{slug}

> **执行方式：** 使用 using-subagents（推荐）或 inline execution 实现本计划。

**目标：** <!-- 一句话，从 design 技术方案概括 -->
**执行模式：** <!-- sequential / parallel / mixed -->

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| <!-- `path/to/file` --> | <!-- 新增/修改/删除 --> | <!-- 一句话说明 --> |

---

## Task 1: <!-- 组件名 -->

**Files:**
- Create: `<!-- exact/path/to/file -->`
- Test: `<!-- tests/exact/path/to/test -->`

- [ ] **Step 1: 写失败测试**

```
<!-- 完整测试代码 -->
```

- [ ] **Step 2: 运行确认失败**

Run: `<!-- 测试命令 -->`
Expected: FAIL — "<!-- 预期错误 -->"

- [ ] **Step 3: 写最小实现**

```
<!-- 完整实现代码 -->
```

- [ ] **Step 4: 运行确认通过**

Run: `<!-- 测试命令 -->`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add <!-- files -->
git commit -m "feat(<!-- scope -->): <!-- 中文描述 -->"
```

---

## Task 2: <!-- 组件名 -->

depends_on: [Task 1]

**Files:**
- Modify: `<!-- exact/path/to/file:行范围 -->`
- Test: `<!-- tests/path -->`

- [ ] **Step 1: 写失败测试**
<!-- 同上格式 -->

---

## 风险与阻塞

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| <!-- 具体风险 --> | <!-- 高/中/低 --> | <!-- 具体措施 --> |

## 决策日志

<!-- 执行过程中记录：日期 — 决策 — 理由 -->

## 完成标准

- [ ] 所有 Task checkbox 勾选
- [ ] 全部测试通过：`<!-- 测试命令 -->`
- [ ] design status 更新为 verified
- [ ] 剩余债务记录到 `tech-debt-tracker.md`
