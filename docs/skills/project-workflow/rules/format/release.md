# Release 文档规格

release.md 是版本快照，回答：**这个版本做了什么、怎么发布、怎么回退**。

## 必填章节

1. **版本摘要**（一段话，30 秒内理解核心变更）
2. **Changelog**（Features / Fixes / Misc，每条关联需求 slug）
3. **包含需求表**（slug、概述、变更类型、影响模块、关联 commits）
4. **独立提交**（不属于需求的零散修复）
5. **变更范围总览**（接口变更、数据变更、依赖变更）
6. **发布与回滚**
7. **关键决策**
8. **已知遗留**
9. **验证状态**（所有 checkbox 须勾选）

## frontmatter

```yaml
version: ""
date: YYYY-MM-DD
retain_until: YYYY-MM-DD
previous_version: ""
next_version: ""
```

## 信息来源映射

| release 中的 | 从哪里提取 |
|--------------|-----------|
| 版本摘要 | 各需求 spec 的"问题与动机" |
| 包含需求表 | 各需求 spec/design 的 frontmatter + 影响范围 |
| 变更范围 | 各需求 design 的"影响范围"、"迁移与兼容" |
| 发布与回滚 | 各需求 design 的"发布与回滚" |
| 关键决策 | 各需求 plan 的"决策日志" |
| 已知遗留 | 各需求 plan 的"风险与阻塞"中未解决项 |
