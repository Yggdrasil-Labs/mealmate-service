# 活跃需求索引

> 当前迭代中所有进行中的需求。每个需求是一个子目录，包含 spec.md / design.md / plan.md。

| id | 需求名 | status | owner | tags | 路径 |
|----|--------|--------|-------|------|------|
| uc3 | 生成周计划 | spec-draft | — | mealplan, core | [uc3-weekly-meal-plan/](./uc3-weekly-meal-plan/) |

## 如何添加新需求

按 `docs/skills/project-workflow/SKILL.md` 的分级规则判断任务级别后：

**中任务**：
1. 创建目录 `docs/active/{slug}/`
2. 从 `docs/skills/project-workflow/templates/requirement/` 复制 `design.md` 和 `plan.md`
3. 填充 frontmatter，在上方索引表中添加条目

**大任务**：
1. 创建目录 `docs/active/{slug}/`
2. 从 `docs/skills/project-workflow/templates/requirement/` 复制 `spec.md`、`design.md` 和 `plan.md`
3. 填充 frontmatter，在上方索引表中添加条目
