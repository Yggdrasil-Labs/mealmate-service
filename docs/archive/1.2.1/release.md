---
version: 1.2.1
date: 2026-05-26
retain_until: 2027-05-26
previous_version: null
next_version: null
---

# 版本 1.2.1 归档

## 版本摘要

本版本完成菜品库管理（UC2），为家庭饮食计划链路提供结构化菜品输入能力。新增 Recipe 聚合及配套 CRUD/搜索 API，支持食材、步骤、营养信息的独立维护，以及逻辑删除后同名复用。

## 包含需求

| slug | 概述 | 变更类型 | 影响模块 |
|------|------|----------|----------|
| uc2-recipe | 菜品库管理：菜品 CRUD、食材/步骤/营养维护、分页搜索、状态流转 | 新增功能 | domain, app, adapter, infrastructure, start |

## 变更范围总览

### 接口变更

新增 `/api/recipes` 系列 REST API（10 个端点），无已有接口变更。

### 数据变更

新增 Flyway migration `V4__create_recipe_tables.sql`，创建 4 张表：
- `recipe`
- `recipe_ingredient`
- `recipe_step`
- `recipe_nutrition`

### 依赖变更

无新增外部依赖。

## 发布与回滚

- **发布策略**：随后端版本全量发布；Flyway 自动应用 V4 migration。
- **回滚方案**：代码回滚到上一版本；如数据库已迁移且无生产数据，可人工删除 UC2 四张表；如已有生产数据，保留表并停止暴露 UC2 API。

## 关键决策

- 采用四表聚合持久化 — 菜品、食材、步骤和营养信息需要被后续计划、采购和营养复盘结构化消费。
- 逻辑删除主表并清理子表 — 保留删除记录以支持同名复用，同时避免子表孤儿数据影响聚合查询。
- 使用 `mock-maker-subclass` — 当前 WSL/JDK 环境无法稳定支持 Mockito inline mock maker 自附加。

## 已知遗留

- 系统预置菜品导入尚未实现（保留 `source_type` 字段，后续独立需求接入）。
- 默认 Surefire 命名规则不自动执行 `*IntegrationTest`，需显式指定。
