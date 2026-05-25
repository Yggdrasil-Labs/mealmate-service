# 自动生成文档注册表

本目录下的文档由智能体从项目源码自动生成，**禁止手动编辑**。

每个文档必须包含 `最后生成: YYYY-MM-DD` 时间戳头。过期（> 30 天）时智能体应重新生成。

## 注册表

| 文档 | 数据源 | 提取方式 | 触发时机 |
|------|--------|----------|----------|
| `db-schema.md` | Flyway migration 文件 + DO 类 | 扫描 `mealmate-start/src/main/resources/db/migration/` DDL 和 `mealmate-infrastructure/src/main/java/**/dataobject/` | bootstrap + schema 变更后 |
| `api-routes.md` | Controller 文件 | 扫描 `mealmate-adapter/src/main/java/**/web/` 下 `@RequestMapping` 注解 | 路由变更后 |

## 如何添加新的生成文档

1. 在上方注册表中添加一行
2. 生成时必须包含 `最后生成: YYYY-MM-DD` 时间戳头和 `数据源:` 声明
3. 智能体下次 gardening 时会按注册表生成

## 判断规则

- 数据源不存在 → 跳过
- 数据源存在但文档不存在 → 生成
- 文档过期（> 30 天）→ 重新生成
- 数据源不再存在 → 删除文档，从注册表移除
