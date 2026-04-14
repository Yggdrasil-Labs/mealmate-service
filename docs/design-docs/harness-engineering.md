# Harness Engineering 体系

## 目标

让仓库成为 AI 可持续理解和决策的系统记录，而不是只依赖零散上下文和即时对话。

## 文档分层

- `AGENTS.md`：AI 工作协议与编码边界。
- `ARCHITECTURE.md`：当前认可的工程事实。
- `docs/product-specs/`：产品规格和需求定义。
- `docs/design-docs/`：长期设计知识。
- `docs/exec-plans/`：执行计划与技术债追踪。
- `docs/references/`：外部知识摘要。
- `docs/generated/`：机器可生成事实快照。

## AI 默认阅读路径

1. 先看 `AGENTS.md`
2. 再看 `ARCHITECTURE.md`
3. 再看相关 `product-specs`
4. 再看相关 `design-docs`
5. 如需落地，再看 `exec-plans`

## 维护约定

- 新需求先补规格，再补设计或计划。
- 跨模块大改动先有计划，再有实现。
- 外部资料不能直接当作内部事实，需要先沉淀到仓库文档。
- 若某一类知识长期反复引用，应从对话中升级进文档。

