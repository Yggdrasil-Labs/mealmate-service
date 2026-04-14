# MealMate 架构总览

## 文档定位

本文件描述 MealMate 当前认可的工程事实，重点覆盖技术栈、技术边界、模块分层、依赖方向和关键对象流转。

本文件不承载：

- 某次需求的临时执行步骤。
- 单次讨论中的候选方案。
- 未确认的未来设想。

这些内容分别进入 `docs/exec-plans/`、`docs/design-docs/` 与 `docs/product-specs/`。

## 1. 系统目标

MealMate 是一个围绕家庭饮食规划与执行闭环构建的后端服务。核心目标不是“展示菜谱”，而是支持以下业务链路：

1. 维护家庭成员画像与饮食约束。
2. 管理菜品、标签、食材与营养信息。
3. 生成一周三餐计划。
4. 派生采购清单与备菜计划。
5. 记录实际饮食情况。
6. 输出营养分析与后续优化建议。

## 2. 技术栈

- Java 17
- Spring Boot 3.3.x
- Maven 多模块聚合工程
- COLA 5.0 DDD 分层架构
- MyBatis-Plus + `@AutoMybatis`
- MapStruct
- Jakarta Validation
- MySQL
- Redis
- Spotless + Google Java Format AOSP

本地环境约定：

- WSL 环境如需使用 Node，请先执行 `source ~/.nvm/nvm.sh`
- JDK 版本为 `openjdk 17.0.10`
- 优先使用 `./mvnw`

## 3. 模块结构

根 `pom.xml` 当前维护以下模块：

- `mealmate-client`：外部契约层，可选。
- `mealmate-adapter`：协议适配层。
- `mealmate-app`：应用层。
- `mealmate-domain`：领域层。
- `mealmate-infrastructure`：基础设施层。
- `mealmate-start`：启动与装配层。

## 4. 分层职责

### `client`

- 定义跨服务或对外暴露的契约。
- 放 Feign、Dubbo 或未来跨服务协议接口。
- 不承担内部业务编排逻辑。

### `adapter`

- 接收 HTTP、RPC、MQ 等入站请求。
- 完成参数校验、协议适配和 DTO 转换。
- 不承载业务规则，不直接依赖 `domain` 与 `infrastructure` 实现细节。

### `app`

- 负责用例编排、事务边界、执行顺序和幂等控制。
- 通过 AppService、CmdExe、QryExe 组织能力。
- 可以依赖 `domain`，不应直接下沉技术实现细节。

### `domain`

- 承载核心业务规则、聚合、实体、值对象、领域服务和仓储接口。
- 不依赖 `adapter`、`app`、`infrastructure`。
- 不直接耦合表结构、Controller、Mapper、外部协议 DTO。

### `infrastructure`

- 实现仓储、网关、持久化和技术配置。
- 封装数据库对象、外部系统调用和底层技术细节。
- 通过实现 `domain` 接口完成依赖倒置。

### `start`

- 聚合模块依赖并负责 Spring Boot 启动。
- 承载全局装配和技术配置入口。
- 不写业务逻辑。

## 5. 技术边界

### 必须遵守的边界

- `adapter` 只处理协议适配、参数校验和返回结构。
- `app` 只处理业务编排、事务边界和执行顺序。
- `domain` 承载业务规则，不依赖 Web、数据库表结构和外部协议 DTO。
- `infrastructure` 实现仓储、网关和持久化，不反向污染领域模型。
- `start` 只负责装配和启动，不承载业务实现。

### 命名与契约边界

- 基础设施层转换器统一使用 `InfraConvertor` 后缀。
- 采购清单统一命名为 `ShoppingList`。
- 提醒任务统一命名为 `NotifyTask`。
- 根包路径统一为 `io.yggdrasil.labs.mealmate`。

### 事务与校验边界

- 事务优先放在 `app` 层入口或执行器。
- 参数校验放在 `adapter` 和 `app` DTO。
- 业务规则校验必须落在 `domain`。

## 6. 依赖方向

允许的主要依赖关系：

- `start -> adapter, infrastructure`
- `adapter -> app`
- `app -> domain`
- `infrastructure -> domain`
- `client` 作为独立契约层存在

默认禁止：

- `adapter -> domain`
- `adapter -> infrastructure`
- `app -> infrastructure`
- `domain -> app/adapter/infrastructure/client`

## 7. 核心业务上下文

当前仓库重点覆盖以下上下文：

- `Family Context`：家庭成员、偏好、忌口、权限和画像配置。
- `Recipe Context`：菜品、配方、标签、营养信息和适配人群。
- `MealPlan Context`：周计划、餐次安排、生成规则和重复校验。
- `Preparation Context`：采购清单、备菜计划和保存方式。
- `Record Context`：照片记录、备注、计划关联和差异比对。
- `Nutrition Context`：营养报告、达标校验和建议输出。
- `Notification Context`：提醒配置、定时任务和消息投递。

更详细的业务定义见：

- [设计文档索引](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/docs/design-docs/index.md)
- [领域上下文](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/docs/design-docs/domain-context.md)

## 8. 对象流转

默认写入链路：

`Request DTO -> App Cmd/Qry -> Domain Entity -> DO -> Database`

默认返回链路：

`Database -> DO -> Domain Entity -> CO -> Response`

其中：

- Adapter Convertor：协议对象转 App DTO。
- App Convertor：Command/Query 转领域对象。
- App Assembler：领域对象转 CO。
- Infra Convertor：Entity 与 DO 互转。

## 9. 验证与构建约定

- 默认使用 `./mvnw` 进行构建和测试。
- 涉及单模块改动时，优先运行最小必要验证。
- 涉及公共契约、模块依赖、POM、启动装配时，优先做更大范围验证。
- 指定单测且使用 `-am` 时，默认补 `-Dsurefire.failIfNoSpecifiedTests=false`。

常用命令：

```bash
./mvnw clean verify
./mvnw spring-boot:run -pl mealmate-start -am
./mvnw -Pdev clean verify
```

## 10. 文档阅读顺序

首次进入仓库时，建议按以下顺序阅读：

1. `AGENTS.md`
2. `ARCHITECTURE.md`
3. [产品规格索引](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/docs/product-specs/index.md)
4. [设计文档索引](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/docs/design-docs/index.md)
5. [执行计划说明](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/docs/PLANS.md)
