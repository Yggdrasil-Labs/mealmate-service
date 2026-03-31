# MealMate Service

MealMate（餐伴）业务后端服务，面向家庭饮食规划场景，支持从家庭画像、菜品管理、周计划生成，到备菜采购、实际记录和营养复盘的完整业务闭环。

## Why MealMate

MealMate 要解决的不是“菜谱太少”，而是家庭日常做饭这件事里更真实的问题：

- 家庭成员饮食偏好不同，宝宝、成人、减脂人群的约束不一样。
- 每周都要重复做“吃什么、买什么、怎么备菜”的决策。
- 计划和实际经常偏离，缺少可追溯的记录与复盘依据。
- 后续想做营养分析、提醒推送、AI 辅助时，需要稳定的业务模型支撑。

因此，这个仓库聚焦的是“家庭饮食规划与执行”后端能力，而不是单纯的菜谱展示系统。

## Core Features

当前业务设计围绕以下能力展开：

- 家庭画像管理：维护家庭成员、口味偏好、忌口、饮食目标和权限边界。
- 菜品中心：维护菜品、食材、标签、适配人群、营养信息和做法说明。
- 周计划生成：基于家庭画像、菜品库和规则生成一周三餐安排。
- 备菜与采购：从周计划派生采购清单、备菜任务和保存说明。
- 实际饮食记录：记录真实用餐照片、备注，并关联计划餐次。
- 营养分析与复盘：对计划与实际进行对比，输出营养评估和优化建议。
- 提醒与推送：支持备菜提醒、采购提醒和固定时点消息任务。

## Scope

当前仓库主要承载 MealMate 的后端业务能力与工程骨架，重点覆盖：

- 家庭画像、菜品、周计划、备菜采购、饮食记录、营养分析等核心链路。
- 基于 COLA 5.0 的领域建模和应用编排。
- 面向 Web 端的接口支撑，并为 App、AI、微前端和 RPC 契约预留扩展能力。

当前阶段不直接交付的内容包括：

- App 原生相机能力，例如闪光灯控制、自动对焦调节。
- 完整的微前端主应用集成，仅保留接入预留。
- 由 AI 直接替代领域规则的决策链路。

## Business Context

MealMate 的核心业务闭环可以概括为：

1. 建立家庭成员画像与饮食约束。
2. 管理标准化菜品知识库。
3. 生成一周用餐计划。
4. 派生采购清单与备菜计划。
5. 记录实际饮食情况。
6. 对比计划与实际，生成营养分析和后续优化建议。

围绕这条主链路，仓库当前重点关注以下领域：

- `Family`：家庭成员、偏好、忌口、权限和画像配置。
- `Recipe`：菜品、配方、标签、营养信息和适配人群。
- `MealPlan`：周计划、餐次安排、生成规则和重复校验。
- `Preparation`：采购清单、备菜计划和保存方式。
- `Record`：照片记录、备注、计划关联和差异比对。
- `Nutrition`：营养报告、达标校验和建议输出。
- `Notification`：提醒配置、定时任务和消息投递。

更详细的业务文档见：

- [业务与领域上下文](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/docs/business/业务与领域上下文.md)
- [范围与实施约束](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/docs/business/范围与实施约束.md)

## Tech Stack

- Java 17
- Spring Boot 3.3.x
- Maven Wrapper
- COLA 5.0 DDD
- MyBatis-Plus
- MapStruct
- Jakarta Validation
- Redis
- MySQL

技术架构是实现手段，不是 README 首页重点。如果需要了解模块分层、依赖方向和编码约束，请查看下方文档导航。

## Project Structure

```text
mealmate-service
├── mealmate-client
├── mealmate-adapter
├── mealmate-app
├── mealmate-domain
├── mealmate-infrastructure
└── mealmate-start
```

## Quick Start

### Requirements

- JDK 17
- Maven Wrapper（优先使用 `./mvnw`）
- MySQL 8.x
- Redis

WSL 环境如需使用 Node，请先执行：

```bash
source ~/.nvm/nvm.sh
```

### Run Locally

1. 配置 `mealmate-start/src/main/resources/application-*.yml` 中的数据源、Redis 和相关环境参数。
2. 启动服务：

```bash
./mvnw spring-boot:run -pl mealmate-start -am
```

### Common Commands

```bash
# 全量校验
./mvnw clean verify

# 快速启动
./mvnw spring-boot:run -pl mealmate-start -am

# 指定 profile
./mvnw -Pdev clean verify
```

## Documentation

- [业务与领域上下文](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/docs/business/业务与领域上下文.md)
- [范围与实施约束](/home/yangyang/workspace/codes/Yggdrasil-Labs/mealmate-service/docs/business/范围与实施约束.md)

如果你要了解更细的工程规范、分层边界和提交约束，可以继续阅读：

- `AGENTS.md`
- 各模块下的 `package-info.java`

## Development Notes

- 这是一个明确的 COLA 5.0 DDD 项目，业务规则应优先沉淀在 `domain`。
- `adapter` 只做协议适配，`app` 做业务编排，`infrastructure` 做技术实现。
- 基础设施层转换器请使用 `InfraConvertor` 后缀，避免与应用层 `Convertor` 重名。
- 默认在当前分支开发，除非明确要求，不主动创建 Worktree、提交或推送代码。
- 提交信息遵循 Conventional Commits。

## Roadmap

- 完成核心后端骨架与数据库落库。
- 打通菜品、周计划、备菜、记录主链路。
- 增加营养分析、提醒推送和历史复盘能力。
- 为 AI、App 和跨服务契约扩展保留稳定边界。

## Related Links

- [COLA](https://github.com/alibaba/COLA)
- [MapStruct](https://mapstruct.org/)
- [MyBatis-Plus](https://baomidou.com/)
