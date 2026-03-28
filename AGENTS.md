# Midgard Backend Template Agent Guide

本文件面向进入本仓库协作的 AI Agent、自动化脚本与开发者，目标是让协作者在第一时间理解本项目的技术事实、分层边界、编码规范与发布约束，避免写出“能跑但不符合 Midgard 风格”的代码。

## 1. 项目事实

### 1.1 技术栈

- Java 17
- Spring Boot 3.3.x
- Maven 多模块聚合工程
- COLA 5.0 DDD 分层架构
- MyBatis-Plus + `@AutoMybatis`
- MapStruct
- Jakarta Validation
- Spotless + Google Java Format AOSP

### 1.2 模块结构

根 `pom.xml` 当前维护以下模块：

- `client`：外部契约层，可选
- `adapter`：协议适配层
- `app`：应用层
- `domain`：领域层
- `infrastructure`：基础设施层
- `start`：启动层

### 1.3 本地环境事实

- WSL 环境下如需使用 Node，请先 `source ~/.nvm/nvm.sh`
- JDK 版本为 `openjdk 17.0.10`
- 优先使用 Maven Wrapper：`./mvnw`

## 2. 第一原则

Agent 在本仓库工作时，必须优先服从以下原则：

1. 先尊重 COLA 5.0 分层，再实现功能。
2. 先维护领域边界，再追求编码速度。
3. 先遵守已有命名与包结构，再新增抽象。
4. 先使用仓库已有能力，再引入新框架或新风格。
5. 先保证提交信息和版本语义可被发布流水线识别，再考虑“描述好看”。

如果实现方案与这些原则冲突，应主动调整方案，而不是绕开规范。

## 3. Cola5 架构风格

本项目是明确的 COLA 5.0 DDD 模板，不是普通分层 Spring Boot 项目。任何改动都必须保持以下边界。

### 3.1 各层职责

#### `client`

- 定义对外契约。
- 放 Dubbo/Feign/跨服务调用接口与协议专属 DTO。
- 只定义，不实现业务。
- 不承载内部编排 DTO。

#### `adapter`

- 处理 Web、mobile、wap、RPC、MQ 等入站协议适配。
- 负责 Request/协议 DTO 到 App Command/Query 的转换。
- 只做参数接收、校验、协议转换、结果返回。
- 不写业务规则，不直接依赖 `domain` 与 `infrastructure` 实现细节。

#### `app`

- 负责业务编排，不负责核心业务规则。
- 组织用例、事务、调用顺序、幂等等应用层逻辑。
- 推荐通过 AppService + Cmd/Qry Executor 组织能力。
- 可以依赖 `domain`，不应反向下沉到 `adapter` 或直接承载基础设施细节。

#### `domain`

- 领域规则中心。
- 放 Entity、Value Object、Domain Service、Repository 接口、领域事件。
- 不依赖 `adapter`、`app`、`infrastructure`。
- 不直接耦合 Controller、Mapper、数据库表结构、三方协议 DTO。

#### `infrastructure`

- 实现 Repository、Gateway、持久化、第三方集成、技术配置。
- 负责 DO、数据访问、外部系统调用。
- 通过实现 `domain` 定义的接口完成依赖倒置。
- 不把基础设施细节泄漏回领域模型。

#### `start`

- 仅负责启动与装配。
- 聚合模块依赖、Spring Boot 启动、全局技术配置。
- 不写业务逻辑。

### 3.2 依赖方向

允许的主要依赖关系：

- `start -> adapter, infrastructure`
- `adapter -> app`
- `app -> domain`
- `infrastructure -> domain`
- `client` 独立作为外部契约层

默认禁止：

- `adapter -> domain`
- `adapter -> infrastructure`
- `app -> infrastructure`
- `domain -> app/adapter/infrastructure/client`
- 在 `start` 写业务实现

除非是非常明确且经过设计确认的框架级装配，否则不要打破以上依赖方向。

## 4. 包结构与命名规范

命名必须与仓库 README、`package-info.java` 和现有 COLA 风格保持一致。

### 4.1 Adapter 层

推荐按领域组织：

- `adapter.web.{domain}`：Web 控制器
- `adapter.web.{domain}.dto`：请求对象
- `adapter.web.{domain}.convertor`：Web 转换器
- `adapter.rpc.provider`：RPC 提供者
- `adapter.mq.consumer`：消息消费者

命名建议：

- Controller：`{Domain}Controller`
- Web Request：`{Verb}{Domain}Request`
- Web Convertor：`{Domain}WebConvertor`
- RPC Provider：`{Domain}RpcProvider`
- MQ Consumer：`{Domain}MqConsumer`

### 4.2 App 层

推荐结构：

- `{aggregate}`：按聚合根分包
- `dto.cmd`：写模型
- `dto.qry`：读模型
- `dto.co`：输出对象
- `dto.enums`：应用层枚举/错误码
- `application`：AppService
- `executor`：CmdExe / QryExe
- `convertor`：Cmd -> Entity
- `assembler`：Entity -> CO

命名建议：

- Command：`{Verb}{Domain}Cmd`
- Query：`{Verb}{Domain}Qry`
- Client Object：`{Domain}CO`
- AppService：`{Domain}AppService`
- AppService 实现：`{Domain}AppServiceImpl`
- Command Executor：`{Domain}{Action}CmdExe`
- Query Executor：`{Domain}{Action}QryExe`
- App Convertor：`{Domain}Convertor`
- Assembler：`{Domain}Assembler`

### 4.3 Domain 层

推荐结构：

- `{aggregate}.model`
- `{aggregate}.service`
- `{aggregate}.repository`
- `{aggregate}.event`

命名建议：

- Entity：`{Domain}`
- Value Object：按业务语义命名
- Domain Service：`{Domain}DomainService`
- Repository 接口：`{Domain}Repository`
- Domain Event：`{Domain}{Action}Event`

### 4.4 Infrastructure 层

推荐结构：

- `persistence.dataobject`
- `persistence.convertor`
- `persistence.impl`
- `gateway`
- `config`

命名建议：

- DO：`{Domain}DO`
- Infra Convertor：优先使用 `{Domain}InfraConvertor`
- Repository 实现：`{Domain}RepositoryImpl`
- Gateway 实现：`{External}GatewayImpl`
- 配置类：`{Tech}Config`

注意：仓库历史中已经明确修复过“infra 层与 app 层转换器命名重复”的问题，因此基础设施层转换器请显式使用 `InfraConvertor` 后缀，避免与 App 层 Convertor 重名。

## 5. 对象与数据流规范

默认转换链路：

`Request DTO -> App Cmd/Qry -> Domain Entity -> DO -> Database`

返回链路：

`Database -> DO -> Domain Entity -> CO -> Response`

职责要求：

- Adapter Convertor：协议对象转 App DTO
- App Convertor：Command/Query 转领域对象
- App Assembler：领域对象转 CO
- Infra Convertor：Entity 与 DO 互转

禁止做法：

- Controller 直接操作 Entity/DO
- Repository 直接返回 Request/CO
- DO 中混入业务行为
- Entity 中出现数据库注解、Mapper 语义或 API 协议字段

## 6. 校验、事务与异常

### 6.1 参数校验

- Adapter 层入口使用 `@Validated` / `@Valid`
- App 层 DTO 使用 Jakarta Validation 注解
- Domain 层负责业务规则校验，不能只依赖外层参数校验

### 6.2 事务边界

- 事务优先放在 App 层的用例编排入口或 Executor
- Domain 层不承担 Spring 事务注解职责
- 不要在 Controller 或 Repository 实现里随意扩散事务边界

### 6.3 异常处理

- 技术异常应在合适边界转换，不把底层实现细节直接暴露到上层
- 业务异常语义应靠近领域表达
- Controller 不要写大段 `try/catch` 充当业务流程控制

## 7. 代码风格

### 7.1 Java 风格

- 遵循 Spotless 自动格式化结果，不与格式化工具对抗
- 当前格式化基于 Google Java Format AOSP
- 默认使用 4 空格缩进
- 清理未使用 import

### 7.2 设计风格

- 优先小而清晰的类与方法
- 避免在 Adapter/Start 写业务逻辑
- 避免“万能工具类”侵蚀领域表达
- 不为模板项目引入与当前技术栈不一致的重量级框架

### 7.3 注释风格

- 注释解释“为什么”，少解释“代码正在做什么”
- 对公共边界、分层约束、非常规设计可写少量说明
- 不写陈旧、口号式或与代码事实不一致的注释

## 8. 持久化规范

- 使用 MyBatis-Plus
- DO 放在 `infrastructure.persistence.dataobject`
- Mapper 由 `@AutoMybatis` 生成时，不要重复手写等价样板
- Repository 实现放在 `infrastructure.persistence.impl`
- 查询条件优先使用类型安全方式组织，避免硬编码字段名

注意：

- DO 关注表结构，不承载领域行为
- Enum 在 DO 与 Entity 的表达可以不同，但必须通过 Convertor 显式转换
- 日期时间优先使用现代时间类型

## 9. Maven 与构建规范

### 9.1 基本要求

- 优先使用 `./mvnw`
- 根版本由 `pom.xml` 的 `<revision>` 统一管理
- 开发期版本保持 `x.y.z-SNAPSHOT`
- 不要手动在各子模块写死独立版本

### 9.2 常用命令

- 全量编译：`./mvnw clean verify`
- 快速启动：`./mvnw spring-boot:run -pl start -am`
- 指定 profile：`./mvnw -Pdev clean verify`

### 9.3 Agent 约束

- 改动后优先运行与改动范围匹配的最小验证命令
- 若修改了公共契约、编译配置、POM、发布脚本，优先做全量编译校验
- 未经明确需要，不擅自改动发布工作流与版本脚本

## 10. Conventional Commits

本仓库的发布流水线依赖 Conventional Commits 与 Release Please，提交信息不是“参考建议”，而是自动发布输入。

### 10.1 提交格式

推荐格式：

`type(scope): summary`

如果没有合适 scope，可使用：

`type: summary`

### 10.2 常用类型

- `feat`: 新功能，会触发 Minor 版本提升
- `fix`: 缺陷修复，会触发 Patch 版本提升
- `perf`: 性能优化，通常会触发 Patch
- `refactor`: 重构，通常不单独触发发布，除非伴随功能/修复语义
- `docs`: 文档更新，不触发发布
- `test`: 测试相关
- `build`: 构建系统、依赖、打包逻辑
- `ci`: CI/CD 工作流
- `chore`: 杂项维护

### 10.3 Breaking Change

发生不兼容变更时，必须显式声明：

- 在 commit body 中包含 `BREAKING CHANGE: ...`
- 或使用带破坏性语义的 Conventional Commit 写法

这会触发 Major 版本提升。

### 10.4 提交示例

- `feat(app): add customer create command executor`
- `fix(infrastructure): avoid duplicate convertor naming`
- `docs: refine agent guide for cola5 conventions`
- `ci(release): adjust release-please version extraction`

## 11. 语义化版本

本仓库采用 SemVer，并通过 `release-please`、`CHANGELOG.md`、`.github/workflows` 和根 `pom.xml` 的 `<revision>` 协同管理。

### 11.1 版本语义

- `MAJOR`：不兼容变更
- `MINOR`：向后兼容的新功能
- `PATCH`：向后兼容的问题修复

### 11.2 当前管理方式

- 开发阶段使用 `*-SNAPSHOT`
- Release PR 会把版本从 `x.y.z-SNAPSHOT` 提升为正式版 `x.y.z`
- 正式发布后，流水线会自动推进到下一个 `x.y.(z+1)-SNAPSHOT`

### 11.3 Agent 注意事项

- 不要随意手工修改 `CHANGELOG.md` 的版本块顺序或发布格式
- 不要在没有发布语义的改动中擅自升级 `<revision>`
- 若任务涉及版本或发布，只在明确理解 `.github/Workflow.md` 与工作流后再修改

## 12. AI Agent 工作守则

### 12.1 变更前

- 先确认改动落在哪一层
- 先确认是否已有同类命名/分包约定
- 先确认是业务规则、应用编排还是协议适配

### 12.2 实现时

- 优先沿用已有模块和风格
- 尽量最小化改动面
- 不跨层偷依赖
- 不为了省事把业务逻辑塞进 Controller、Config、Repository
- 默认直接在当前分支上开发
- 未经用户明确要求，不主动创建或切换到 Git Worktree

### 12.3 提交前

- 至少做编译级验证
- 确认格式化无问题
- 确认新增命名符合本文件约束
- 确认提交信息符合 Conventional Commits
- 确认改动语义与预期版本语义一致
- 默认不自行执行 `git commit`、`git push`、打 tag 或发布动作，除非用户明确要求

## 13. 对 AI 最重要的几条硬约束

如果你是第一次进入本仓库，请优先记住这几条：

1. 这是 COLA 5.0 DDD 模板，不是随意分层的 Spring Boot 项目。
2. `adapter` 只做适配，`app` 只做编排，`domain` 才是业务规则中心，`infrastructure` 负责技术实现，`start` 只做启动装配。
3. `InfraConvertor` 命名要和 App 层 Convertor 区分开，避免重复。
4. 统一使用根 `pom.xml` 的 `<revision>` 管理版本，开发阶段保持 `SNAPSHOT`。
5. Commit 必须符合 Conventional Commits，因为发布系统依赖它生成版本与 `CHANGELOG.md`。
6. 影响版本语义的变更必须匹配 SemVer，不要随意修改发布文件。
7. 优先执行 `./mvnw`，并用最小但足够的验证证明改动有效。
8. 默认不要自行提交代码，只有在用户明确要求时才执行提交、推送或发布相关 Git 操作。
9. 默认在当前分支工作，只有在用户明确要求时才使用 Git Worktree。
