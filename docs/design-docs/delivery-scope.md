# 交付范围与实施边界

## 文档说明

- 目标：明确 MealMate 当前阶段的设计范围、实施边界和工程约束，避免业务设计与研发落地脱节。
- 当前状态：本文件已成为范围与实施边界的唯一主入口。

## 当前范围

当前设计重点覆盖以下内容：

- 家庭画像、菜品中心、周计划、备菜采购、饮食记录、营养分析、消息提醒等主业务链路。
- 后端模块化单体架构、数据库设计、接口契约和部署方案。
- Web 端基础页面与移动优先交互。
- AI、App、微前端、RPC 契约等后续扩展预留。

## 当前不在本阶段直接交付的能力

- App 原生相机能力，例如闪光灯控制、自动对焦调节。
- 完整的微前端主应用集成，只保留 qiankun 兼容预留。
- 直接由 AI 决策替代领域规则，AI 仅输出候选结果。
- 独立部署编排项目内的 Docker Compose、Nginx 编排与环境模板维护。

## 业务实施边界

- 系统应围绕“计划 - 执行 - 记录 - 复盘”闭环建设，不应退化成单纯菜谱管理系统。
- 业务规则优先落在领域层，不应散落在 Controller、Config 或 Repository 实现中。
- 周计划、备菜采购、饮食记录和营养分析应保持同一业务链路上的可追溯性。
- 差异化成员约束是核心能力，宝宝适配和减脂目标不能被简化为普通标签筛选。

## 架构与分层约束

- 后端采用 COLA 5 DDD 分层：`adapter -> app -> domain`，`infrastructure -> domain`，`start` 只做装配。
- `adapter` 不直接依赖 `domain` 或 `infrastructure` 实现细节。
- `app` 负责编排与事务，不直接承载持久化技术细节。
- `domain` 不依赖 Spring Web、数据库表结构、协议 DTO 或三方集成实现。
- `infrastructure` 通过实现仓储与网关接口完成依赖倒置。

## 工程与技术约束

- 后端基线：Java 17、Spring Boot 3.x、Maven 多模块、MyBatis-Plus、MapStruct、Jakarta Validation。
- 数据库首选 MySQL 8.x，热点数据缓存使用 Redis，数据库变更通过 Flyway 管理。
- 统一使用根包路径 `io.yggdrasil.labs.mealmate`。
- Maven 坐标建议保持 `groupId=io.github.yggdrasil-labs`、`artifactId=mealmate-service`。
- 仓库文档统一落在仓库根目录 `docs/` 下，不单独拆分 docs 仓库。

## 命名与契约约束

- 采购清单统一命名为 `ShoppingList`，禁止使用 `PurchaseList`。
- 提醒任务统一命名为 `NotifyTask`，禁止使用 `Reminder` 或泛化 `Task`。
- 基础设施层对象转换器应使用 `InfraConvertor` 后缀，避免与应用层 `Convertor` 混淆。
- 返回结构遵循 COLA 风格：`success`、`errCode`、`errMessage`、`data`。
- 链路追踪字段放入 Header，例如 `X-Trace-Id`、`X-Request-Id`，不进入响应体。

## 前后端实施约束

- Web 端拍照仅支持调起摄像头或相册选图，并支持前端裁剪与上传。
- 裁剪信息应以 JSON 形式落入 `meal_record_photo.crop_info`。
- 前端需兼容独立运行和 qiankun 子应用挂载两种模式。
- 后端接口应按业务域聚合，不以页面实现细节反向驱动领域建模。

## AI 与扩展能力约束

- AI 结果必须经过领域规则校验后才能进入正式业务链路。
- 大模型厂商接入应通过统一网关抽象，避免能力耦合到核心领域。
- Prompt 组装、结果解析和调用审计应作为独立能力封装。
- 后续支持 App、多家庭、库存、跨服务契约时，不应破坏已有核心聚合与统一语言。

## 实施阶段建议

### 第一阶段

- 搭建核心后端骨架与数据库结构。
- 打通菜品、周计划、备菜、记录主链路。
- 完成基础前端页面与接口联调。

### 第二阶段

- 增加营养分析、提醒推送、历史复盘。
- 补齐微前端和 App 接入预留。

### 第三阶段

- 引入 AI 能力。
- 强化跨服务调用契约与多端协同能力。
