# 归档步骤约束

仅在用户明确表示"版本开发完成"时触发。

## 前置条件

- 所有归档需求的 plan status = `completed`
- 所有归档需求的 design status = `verified`
- 用户确认版本号（或接受 agent 基于变更类型推荐的 semver）

## Semver 推荐规则

- 存在破坏性变更（接口删除/不兼容修改）→ major
- 存在新功能 → minor
- 仅修复/重构 → patch

## 归档操作

1. 如 release.md 尚未创建，从 `templates/version/release.md` 复制
2. 按 `format/release.md` 规格填写
3. 将归档需求目录从 `docs/active/` 移动到 `docs/archive/{版本号}/`
4. 更新 `docs/active/index.md`（删除已归档条目）
5. 更新 `docs/archive/index.md`（添加新版本）
6. 如有前序版本，回填其 `next_version` 字段
7. 剩余债务记录到 `docs/active/tech-debt-tracker.md`

## 小任务/bugfix 记录

不走需求流程的小任务和独立 bugfix，在当前版本 release.md 的"独立提交"表中登记。
