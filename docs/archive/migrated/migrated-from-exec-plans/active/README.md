# 进行中的执行计划

本目录存放仍在推进中的实施计划文档。

## 当前计划

| 计划名称 | 文件 | 开始日期 | 状态 | 说明 |
| --- | --- | --- | --- | --- |
| UC2 菜品库实施 | [2026-04-16-uc2-recipe-implementation-plan.md](./2026-04-16-uc2-recipe-implementation-plan.md) | 2026-04-16 | 🚧 进行中 | 菜品库后端实施 |

## 状态说明

- 🚧 **进行中**：计划正在执行
- ⏸️ **暂停**：计划暂时搁置
- ✅ **已完成**：计划完成，待归档

## 命名约定

```
YYYY-MM-DD-<topic>-implementation-plan.md
```

**说明**：
- 只存放实施计划（implementation-plan）
- 设计文档应放在 `design-docs/`
- 调研文档应放在 `design-docs/` 或 `references/`

## 计划应包含的内容

- **目标**：明确要达成什么
- **范围**：明确做什么和不做什么
- **任务分解**：将目标拆分为可验证的小步骤
- **验证方式**：说明如何验证每个任务完成

## 完成后的处理

计划完成后，应：

1. 更新计划状态为"已完成"
2. 补充完成日期和最终结果
3. 将技术债记录到 [tech-debt-tracker.md](../tech-debt-tracker.md)
4. 将计划移动到 [../completed/](../completed/)
5. 更新本 README 的计划列表
