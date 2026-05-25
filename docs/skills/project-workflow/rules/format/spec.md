# Spec 文档规格

spec 只回答：**做什么** 和 **为什么**。

## 边界判断

| 放 spec | 放 design |
|---------|-----------|
| 用户能感知的输入输出 | API 端点、HTTP 状态码、字段类型 |
| 用户感知的响应速度 | 缓存策略、数据库索引 |
| 业务规则 | 实现规则的技术方案 |
| 错误场景的用户体验 | 错误处理策略（重试/熔断） |
| 产品约束 | 技术约束 |

## 必填章节

1. **问题与动机**
2. **目标用户**
3. **功能边界**（In Scope + Out of Scope）
4. **用户场景**（编号步骤，复杂流程用 Mermaid）
5. **输入与输出**（用户视角）
6. **验收标准**（Given/When/Then，每条可测试）
7. **异常与边界情况**
8. **产品约束**
9. **度量与成功标准**

## frontmatter

```yaml
id: spec-{slug}
status: draft | in-progress | shipped
owner: ""
tags: []
created: YYYY-MM-DD
updated: YYYY-MM-DD
```

## 拆分规则

- 能独立上线、独立回滚 → 粒度合适
- 必须和另一个需求同时上线 → 合并
- In Scope 超过 10 条 → 考虑拆分
