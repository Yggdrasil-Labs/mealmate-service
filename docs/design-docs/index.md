# 设计文档索引

## 作用

本目录沉淀 MealMate 的长期设计知识，回答“为什么这样设计”以及“哪些原则比局部实现更重要”。

它不是需求规格目录，也不是执行计划目录。

## 阅读顺序

1. [核心信念](./core-beliefs.md)
2. [领域上下文](./domain-context.md)
3. [交付范围与实施边界](./delivery-scope.md)
4. [Harness Engineering 体系](./harness-engineering.md)

## 文档清单

- `core-beliefs.md`：AI、架构、产品和工程协作中的核心信念。
- `domain-context.md`：核心业务上下文、统一语言和建议聚合。
- `delivery-scope.md`：当前阶段交付范围、边界与实施约束。
- `harness-engineering.md`：仓库文档系统的设计方式与维护约定。

## 更新规则

- 当长期设计原则发生变化时，更新本目录文档。
- 当只是某一项需求的临时步骤变化时，不要修改本目录，而是更新 `docs/exec-plans/`。
- 当需求本身变化时，优先更新 `docs/product-specs/`。

