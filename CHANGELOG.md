# Changelog

## [1.2.0](https://github.com/Yggdrasil-Labs/midgard-backend-template/compare/v1.1.0...v1.2.0) (2026-03-28)


### ✨ Features

* ✨ Feature: 支持容器化构建 ([c64e2a9](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/c64e2a96b50409ecbd75d84d59ed600a94b85f41)), closes [#33](https://github.com/Yggdrasil-Labs/midgard-backend-template/issues/33)


### 🐛 Bug Fixes

* 🐛 Bug: spotless apply应该在maven的process-sources阶段 ([3587828](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/3587828834feb18e1cb82c9066b7e4a0f327ecaf)), closes [#34](https://github.com/Yggdrasil-Labs/midgard-backend-template/issues/34)
* 更新文件权限并改进 Dockerfile 以进行非 root 用户设置 ([a7a926f](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/a7a926fc7dc89a36348841afff0d30291c13d27e))


### 📝 Documentation

* 完善Agents规范 ([5b3c748](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/5b3c748b2ee8de18c5ad8ecc5ba9b673ffcedad5))
* 将 OpenSpec 指令添加到 AGENTS.md，以获取 AI 助手关于提案和更改的指南 ([2769a8a](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/2769a8a0e432a7d49751f2a0f3846709afaa02ba))
* 新增 AI Agent 指令文档 (AGENTS.md)，定义 AI 行为规范和工作流程 ([04580fd](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/04580fd7b7a35dc35267d1f9f7aead71639527e9))
* 更新 README，调整各层职责和命名规范，明确 Client 层与 App 层的关系 ([b653e86](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/b653e86ab76b74238d3261e373a941408d64a589))
* 更新 依赖关系 ([02a5bdd](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/02a5bdd334cfac0510f0b0af017a3c708bbc9596))
* 更新工作流文档，调整触发条件和步骤说明，增加 Docker 镜像发布支持 ([3fa59fb](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/3fa59fb0d8484fe73e1af1fee7de8fe0ad16823b))


### ♻️ Code Refactoring

* 删除不再使用的客户适配器和相关类，更新包结构说明 ([ddc207a](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/ddc207a6078afb6b6f59434a221e27193efe5e5c))
* 调整包结构为io.yggdrasil.labs.midgard ([37218f4](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/37218f496784bac89ffb0c1c3aec35d9c5448279))
* 适配层重构，依赖关系调整为直接依赖应用层，更新相关DTO和命令类以支持新的架构 ([35a6eaf](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/35a6eafdc76423a7167ed200ee574455929c2731))


### 👷 Continuous Integration

* **deps:** bump actions/checkout from 6.0.1 to 6.0.2 ([5fca516](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/5fca516473d7b0bffbca695f6ad147e44ca2db2e))
* **deps:** bump docker/build-push-action from 5 to 6 ([0dcfee4](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/0dcfee41535f2394ae27d90ac4480314de265951))
* **deps:** bump docker/build-push-action from 6 to 7 ([9f1db25](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/9f1db25db9d21475f5587f3f2e7ea47bab3be87d))
* **deps:** bump docker/login-action from 3 to 4 ([b387f0d](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/b387f0d912fff7dd4acf595fcfecf2e180917fbb))
* **deps:** bump docker/metadata-action from 5 to 6 ([b01fa34](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/b01fa342ff45380ad7ed0efc309ac5c548cbb55c))
* **deps:** bump docker/setup-buildx-action from 3 to 4 ([74d9be1](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/74d9be142494180c884fe2c664fc654f00bf2968))
* **deps:** bump docker/setup-qemu-action from 3 to 4 ([7889c81](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/7889c81218352a52dfd88b7ccfb985e5d087ef08))
* **deps:** bump googleapis/release-please-action from 4.2.0 to 4.4.0 ([7ec68a7](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/7ec68a78fc81c3d33eef35e97b7b0c6bb976534a))
* normalize Docker image name in release workflow ([4b9f42d](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/4b9f42db4c9fd92e851ada87e36b7e2c48a8bc21))
* **release:** 支持将镜像发布到gpr ([17911b4](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/17911b4bb445df63059e58ab718ce3b6dc0559ec))
* 修改判断逻辑，避免误升级 ([fb1e202](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/fb1e2029d3c5463f822dbdfa07c2efcc1424886d))


### 🔧 Miscellaneous Chores

* active profile local ([ae81e49](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/ae81e49ded3f89a4a68599b091fe7aa3731bcc89))
* bump version to 1.1.1-SNAPSHOT for next development cycle ([58117f6](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/58117f60f2087738bc67291de202e7dba5a5fe30))
* **deps-dev:** bump com.diffplug.spotless:spotless-maven-plugin ([7c79d1a](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/7c79d1a27256716f4eda07ee4731e79fd6594ef2))
* **deps-dev:** bump com.diffplug.spotless:spotless-maven-plugin ([a57e5a7](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/a57e5a7d90b186854e98162656409cd5463f144a))
* **deps-dev:** bump com.diffplug.spotless:spotless-maven-plugin ([404698f](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/404698f25234ac4c01457086d62a4c99620d3a29))
* **deps:** bump io.github.yggdrasil-labs:mimir-boot-bom ([29b6fc8](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/29b6fc88474ff168c1cdf3fee0c891678930b582))
* **deps:** bump io.github.yggdrasil-labs:mimir-boot-parent ([8e182aa](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/8e182aa5c4e9b2a07bb15f7daffeef544ca71cd4))
* **deps:** 调整项目的依赖关系 ([bed5557](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/bed55571488fed8395a0f5baa0e4450202b9a955))
* 升级parent到2.0.0，修改groupId ([abe6b2a](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/abe6b2ab90a15d4a38c6ef24d17134eb8f56a283))
* 更新依赖项的groupId为io.github.yggdrasil-labs，以统一命名规范 ([a5b845b](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/a5b845b0f037e6466c92b51f8d230f275aa00327))

## [1.1.0](https://github.com/Yggdrasil-Labs/midgard-backend-template/compare/v1.0.0...v1.1.0) (2025-12-10)


### ✨ Features

* 增加db目录 ([04060da](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/04060dac1d8f85a8068f5a3ac6a7a827c33b5eff))


### 🐛 Bug Fixes

* 🐛 Bug: Code界面的README展示错误 ([4594210](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/459421058d20e077cd93f5b5e04477366325bc73)), closes [#18](https://github.com/Yggdrasil-Labs/midgard-backend-template/issues/18)
* 删除数据库信息 ([c38b2fb](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/c38b2fb4484f74d35fe2cdf0fec8948a2e1423ab))
* 避免infra层与app层的转换器命名重复 ([e5c452f](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/e5c452f799496a3ac7dc3728f601445f37d1a293))


### 📝 Documentation

* 新增工作流的相关文档 ([36765b3](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/36765b3d3f0f5d71a6f42f4bc6d194545754761d))
* 更新文档 ([9144473](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/91444735af2d3a7b98e575a21abd0eea0f0524e2))
* 更新文档引用 ([d79cfae](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/d79cfae05118634bbddd71a31916ed71c0803213))


### ✅ Tests

* 移除start层的test代码 ([b2027e1](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/b2027e13f544514d80d39d7b3a22d23b366be029))


### 👷 Continuous Integration

* action的依赖升级 ([83f6fef](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/83f6fef46673d304a15bff637d94ef81e80f721f))
* **cerate-tag:** 清除 checkout 步骤设置的 GITHUB_TOKEN 凭证 ([c392217](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/c3922178c9c1c5e91cc1a1b07f749e55a0839ff0))
* **create-tag:** 不持久化GITHUB_TOKEN ([f3f7f08](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/f3f7f08efc247be2f40a6203949a44f845d8d325))
* **create-tag:** 优化检查 release PR是否已合入的逻辑 ([9c1f654](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/9c1f654af496a0e47040907494964bd5bd48ee8e))
* **create-tag:** 增加手动执行能力，强制使用PAT ([17b53ab](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/17b53abd1920c875cbe7f353eebc81aa6c474db6))
* **release-please:** PR就使用默认的格式，也不需要bootstrap-sha记录上次发布点 ([6f535ca](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/6f535ca8c4e28b1f990a9026ed8d8e2403b9571d))
* **release-please:** 不在工作流中写死仓库名 ([a157927](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/a1579271198add469e8fa6613ad6e7b461b3840b))
* **release-please:** 修复没打自定义标签问题 ([2737674](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/273767480d04dbd3302d9f53441e7048029a424b))
* **release-please:** 去掉package-name，PR的title默认会使用当前仓库名 ([320a31d](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/320a31df13ffc9256f8c536244985b3f072be5e0))
* **release-please:** 指定PR的title模板 ([2f8a728](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/2f8a728a9c1b4af891d6b19d1b9b7dbcbd63f1d4))
* **release-please:** 正确获取pom的reversion ([350cf12](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/350cf12d3dc9555d2420dbec58f4ac7f52e8aa2c))
* **release-please:** 禁用自动创建Github Release/Tag ([f914e42](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/f914e424d562834080c155ecc3570f54b892fd01))
* **release-please:** 简化流水线，修复没打自定义标签问题 ([a69f1de](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/a69f1de7cebe3cd45d4fae4885c9d8d4a3a57899))
* **release-please:** 自动重试打标签 ([b4e2df3](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/b4e2df316135aa34f6475f707718cb5c7c5ea908))
* **release:** Release Note 增加Icon ([805c6d6](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/805c6d67aec754d1ce2115122c1d1e26a6e5c009))
* 工作流优化 ([560456d](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/560456d5f308e3a75d91ccb9d1a862d7655ebc4f))
* 简化release please，不需要额外更新revision ([eb56125](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/eb561258bd74eb984ca71d94d56f95ba31f92736))


### 🔧 Miscellaneous Chores

* 修正bootstrap-sha ([4baf74d](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/4baf74dc303125c4f068b0a3a8b11d02502a7728))
* 忽略 application-local.yml ([71e1021](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/71e10211f260026ef7f9f95b1a2de5e62449d80d))
* 校验API依赖升级 ([3702b6a](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/3702b6a994df496435f8284ce6632cfa2c972f28))
* 统一author ([62bb6eb](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/62bb6ebfff443deaa6eb3aa66fa9388bf95f17fc))

## 1.0.0 (2025-12-06)


### Bug Fixes

* add missing import for Application class in TestApplication ([d23e2c2](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/d23e2c2f242285d81cd63aaffc601faf534b9165))
* 修复编译错误 ([0c36ddb](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/0c36ddb381c778a4657df97645dab964fe1c85fe))


### Code Refactoring

* 代码优化 ([45d1e34](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/45d1e3422f51b13b239d67321feb9e8f3ad915d1))
* 优化类命名与分包策略 ([26546ea](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/26546ea61590598180c354b289e79d08532da389))
* 优化类命名与分包策略 ([df08de1](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/df08de1880b85cf766a3147913ff2f420f75aa8f))
* 优化类命名与分包策略，完善package-info与README ([487aea2](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/487aea2a82d0b376d9ac9d51c8781a511d5358d0))


### Tests

* 修复编译错误 ([64695ce](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/64695cedba95cf1804953102eb3915dcc6f6c3eb))


### Build System

* 为 Maven Wrapper 添加可执行权限 ([f0585a6](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/f0585a600209d97bb527b154083507f54e5aed44))
* 编译时自动格式化 ([ec13c1c](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/ec13c1c9ecd8bebd20f704fc77c08eb93df82627))


### Continuous Integration

* **ci:** 修复ci报错 ([05a1eb5](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/05a1eb57d361071e7ef76a4255a23fa0527a18e5))
* **ci:** 修复ci报错 ([72c3807](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/72c3807281bd15144ba66c1ecd6bb2ea205286f7))
* **ci:** 修复ci报错 ([a76e9ad](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/a76e9ad45a733cc34868c0350f2d6564d22dc2ed))
* **workflow:** Add GitHub Actions workflow for syncing labels ([0f52a74](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/0f52a7426ba68a7909a4371e34c24285ea5199ac))
* 新增常用工作流 ([0b91abb](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/0b91abb2e6c8611b0e4b387fa158d1aa33822784))
* 流水线调整 ([ddc26a6](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/ddc26a68854b9747185b64b3924e51ca37e9bbff))
* 跳过单元测试 ([76752bf](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/76752bf7385d09896ed75fd34725ec413b5f8a56))


### Miscellaneous Chores

* 更新README、支持mvnw ([b3deda0](https://github.com/Yggdrasil-Labs/midgard-backend-template/commit/b3deda074628904686bfd764313ea6fe9e9ebe13))
