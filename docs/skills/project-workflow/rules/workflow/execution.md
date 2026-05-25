# 执行阶段约束

plan 审查通过且用户确认后，进入实施。

## TDD 强制

没有失败测试不写实现代码。违反则删除实现重来。

## 执行规则

1. 按 Task 顺序执行（除非 parallel 模式）
2. 每个 Task 完成后 commit，message 用 Conventional Commits 中文
3. 遇到决策记录到决策日志：日期 — 决策 — 理由
4. 同一 Task 失败 3 次 → 停下，不是缺努力是缺能力
5. 架构不清晰 → 回到 design，不要边猜边做

## Subagent 调度

- 独立任务 ≥ 3 个 → 使用 subagent 并行
- 每个 subagent 拿到完整 Task 文本 + 必要上下文，不读 plan 全文
- Subagent 完成后做两阶段 review：spec 合规 → 代码质量
- subagent 阻塞 → 换更强模型或拆更细的 Task

## 执行模式决策树

```
任务之间有顺序依赖？ → sequential（单 agent 按序）
任务之间无共享状态？ → parallel（subagent 并行）
混合？ → mixed（先前置，再 fan-out）
```

## 阻塞处理

1. 拆解为更小的构建模块
2. 先构建缺失的基础模块
3. 仍然阻塞 → 暂停，报告"缺少什么能力"

## 完成后

询问用户：
- "继续下一个需求" → 回到 Step 1
- "版本开发完成" → 进入归档
