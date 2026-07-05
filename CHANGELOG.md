# Changelog

## [1.3.0](https://github.com/Yggdrasil-Labs/mealmate-service/compare/v1.2.0...v1.3.0) (2026-07-05)


### ✨ Features

* **mealplan:** Controller 新增调整/推荐/历史端点 ([8ce9674](https://github.com/Yggdrasil-Labs/mealmate-service/commit/8ce96742b62303fda7bf6db5907c01ce8b04d912))
* **mealplan:** 基础设施层实现调整历史持久化 ([54539b8](https://github.com/Yggdrasil-Labs/mealmate-service/commit/54539b84d38d9a145791165357a0b7c66253bd1c))
* **mealplan:** 实现UC3生成周计划完整后端链路 ([e5eb3c6](https://github.com/Yggdrasil-Labs/mealmate-service/commit/e5eb3c6c32a55d45030cd43dac19f4867961252b))
* **mealplan:** 实现不重样校验和推荐领域服务 ([09930a1](https://github.com/Yggdrasil-Labs/mealmate-service/commit/09930a196f365a6b3c831bdf6dc84d5689985551))
* **mealplan:** 应用层实现调整/推荐/历史三个执行器 ([55c875f](https://github.com/Yggdrasil-Labs/mealmate-service/commit/55c875fe92c5e2fcf4b29c2bccab5be300fccd19))
* **mealplan:** 添加餐次调整历史表和字段迁移脚本 ([1eee4a2](https://github.com/Yggdrasil-Labs/mealmate-service/commit/1eee4a23a778d50f6a0b0c50a049bc841a3d1535))
* **mealplan:** 领域层新增调整历史实体和行为方法 ([fa5a488](https://github.com/Yggdrasil-Labs/mealmate-service/commit/fa5a488a23c43d087a97b255c7716797648ad861))
* **recipe:** 实现菜品管理功能 ([44a3a17](https://github.com/Yggdrasil-Labs/mealmate-service/commit/44a3a17c1e7809b5a624b3ba7cbff307e7036b71))
* 合入 UC4 调整餐次菜品(v1.3.0) ([b9d4839](https://github.com/Yggdrasil-Labs/mealmate-service/commit/b9d4839685f06553519e4aa4f0bc4d1057276442))
* 实现UC1的领域层和仓储 ([ef78ecd](https://github.com/Yggdrasil-Labs/mealmate-service/commit/ef78ecd394bb3dc6b031a8371b9a21a6bc18ed15))
* 添加家庭成员管理API及OpenAPI文档支持 ([e3e7e99](https://github.com/Yggdrasil-Labs/mealmate-service/commit/e3e7e99906634bad9811f5b95904103373183b7f))


### 🐛 Bug Fixes

* **mealplan:** AdjustMealItemCmd 移除 path 字段 @NotNull + GetItemHistoryQryExe 增加 planId 归属校验 ([27b5910](https://github.com/Yggdrasil-Labs/mealmate-service/commit/27b5910805c094a2eaba11faba4a5df9dcc66b2d))
* **mealplan:** getCurrentWeekPlan 加载关联计划项，修复 dayMeals 返回空 ([8e1561a](https://github.com/Yggdrasil-Labs/mealmate-service/commit/8e1561a0c7a6ae5bf818ea4377dc77e0e76d7ecc))
* **mealplan:** validateNoDuplicate 异常类型改为 IllegalArgumentException ([12cdd90](https://github.com/Yggdrasil-Labs/mealmate-service/commit/12cdd90606d5bdb2e3c41bf564e14053527998e3))
* **mealplan:** 修复 N+1 查询、新增全局异常处理、补充应用层测试、丰富聚合根行为 ([7d11975](https://github.com/Yggdrasil-Labs/mealmate-service/commit/7d119756d67d7dbb5a0fb805ff4a465b43860f88))
* **mealplan:** 同一餐次内不允许出现重复菜品 ([bd9ae90](https://github.com/Yggdrasil-Labs/mealmate-service/commit/bd9ae90fd093511bebdc1299b45b1f85a37fc6e5))
* **migration:** V6 ALTER TABLE 拆分为 H2 兼容语法 ([6c162d0](https://github.com/Yggdrasil-Labs/mealmate-service/commit/6c162d06c975f29b4428d5c4c103d22c16b302a6))
* **recipe:** 对齐前后端枚举和校验规则 ([ab26f3b](https://github.com/Yggdrasil-Labs/mealmate-service/commit/ab26f3b4b4512a9a6b5020d1e30882de25785516))
* 异常处理器重命名避免 Bean 冲突 + ConfirmPlan 加载完整聚合根 ([7c5349b](https://github.com/Yggdrasil-Labs/mealmate-service/commit/7c5349b39d96e5bb0116e28fd05d572c3bbf7d8a))


### 📝 Documentation

* **mealplan:** UC4 需求文档 spec/design/plan/execution-state ([d7989a1](https://github.com/Yggdrasil-Labs/mealmate-service/commit/d7989a1ebeb9309cd6a58538d10b12b89058f515))
* migrate to harness standard structure ([0ed5a65](https://github.com/Yggdrasil-Labs/mealmate-service/commit/0ed5a65ea52f0b090087852dffc36d01284371a9))
* 同步 UC2 菜品库文档状态 ([f47c23a](https://github.com/Yggdrasil-Labs/mealmate-service/commit/f47c23a57f05fdfec96e44370733569a6403194d))
* 基于Harness engineering构建docs体系 ([3022193](https://github.com/Yggdrasil-Labs/mealmate-service/commit/3022193416a60fb30c82684d3b3ecf566af3dd6c))
* 归档 UC2 并按 harness-docs 标准重构文档体系 ([7572e5d](https://github.com/Yggdrasil-Labs/mealmate-service/commit/7572e5d6119a6f65659f86afcf97482676ee2154))
* 归档 UC3 + UC4 至 v1.3.0 ([f2913b9](https://github.com/Yggdrasil-Labs/mealmate-service/commit/f2913b98abd710ba29114c90c06a75e161e8dd3d))
* 更新 README，新增业务与领域上下文及实施约束文档 ([2c5bf0d](https://github.com/Yggdrasil-Labs/mealmate-service/commit/2c5bf0dff4605abf62caac8a3d25ac69727c8e8c))
* 添加UC1家庭成员设计和实施计划 ([af55425](https://github.com/Yggdrasil-Labs/mealmate-service/commit/af554250b2216373434406d4796a61044c70a36b))


### ♻️ Code Refactoring

* **adapter:** 移除全局异常处理器 ([d173218](https://github.com/Yggdrasil-Labs/mealmate-service/commit/d173218b40ee29efccffb2c1f360c4a4959fcf30))
* **mealplan:** MealPlanAppService 拆分为独立 Executor ([7467492](https://github.com/Yggdrasil-Labs/mealmate-service/commit/74674920e2e588db323260fce3042c76fd538454))
* **mealplan:** 引入 BizException 体系替代裸异常 ([87c01b7](https://github.com/Yggdrasil-Labs/mealmate-service/commit/87c01b7b5c8718e15e1bcef0f555ee63ceec11d7))
* **recipe:** 领域模型富血化 + 引入查询条件值对象 ([1d55c6e](https://github.com/Yggdrasil-Labs/mealmate-service/commit/1d55c6e0c6eac27308fc79bfdefbd4c71a004e19))
* 更新项目模块名称并调整启动命令 ([d72d318](https://github.com/Yggdrasil-Labs/mealmate-service/commit/d72d3183863e0023f66e1ad9f177e9425de4f4b9))
* 替换模板项目的内容 ([7ee151c](https://github.com/Yggdrasil-Labs/mealmate-service/commit/7ee151cca2ba983262f90d1325ed31ddf6ed02a8))


### ✅ Tests

* 配置 Mockito 使用 subclass mock maker ([876600b](https://github.com/Yggdrasil-Labs/mealmate-service/commit/876600bd5bdedec3688c496cf772a39b875d383c))


### 👷 Continuous Integration

* **deps:** bump actions/checkout from 6.0.2 to 7.0.0 ([443d408](https://github.com/Yggdrasil-Labs/mealmate-service/commit/443d408520f180e8e97dc42821bf363fe4b40c5c))
* **deps:** bump actions/checkout from 6.0.2 to 7.0.0 ([e2016c6](https://github.com/Yggdrasil-Labs/mealmate-service/commit/e2016c693e101d2afec95918fc29301166085c45))
* **deps:** bump actions/github-script from 8.0.0 to 9.0.0 ([bc2da87](https://github.com/Yggdrasil-Labs/mealmate-service/commit/bc2da8707be4339d96e721edd99a06a198b316ae))
* **deps:** bump googleapis/release-please-action from 4.4.0 to 4.4.1 ([109d020](https://github.com/Yggdrasil-Labs/mealmate-service/commit/109d020a93ea923cfa7b0f48378c56880db33866))
* **deps:** bump googleapis/release-please-action from 4.4.1 to 5.0.0 ([eb06d1a](https://github.com/Yggdrasil-Labs/mealmate-service/commit/eb06d1a033996f6a6ee032d5886dda7d0bb9e4ad))
* **deps:** bump softprops/action-gh-release from 1 to 2 ([6135d84](https://github.com/Yggdrasil-Labs/mealmate-service/commit/6135d84879ddc21ba7790773a36c720e62910e51))
* **deps:** bump softprops/action-gh-release from 2.6.1 to 3.0.0 ([287bd96](https://github.com/Yggdrasil-Labs/mealmate-service/commit/287bd96a941ce45b0ab1a2cbd968257ae0b5e588))
* **deps:** bump softprops/action-gh-release from 3.0.0 to 3.0.1 ([b035145](https://github.com/Yggdrasil-Labs/mealmate-service/commit/b035145485594a1c68141313d9b6411df83426c5))
* **deps:** bump softprops/action-gh-release from 3.0.0 to 3.0.1 ([a4672ad](https://github.com/Yggdrasil-Labs/mealmate-service/commit/a4672ad742c7556d810d5965bd0fcfa3c54e7a34))


### 🔧 Miscellaneous Chores

* **deps-dev:** bump com.diffplug.spotless:spotless-maven-plugin ([2585673](https://github.com/Yggdrasil-Labs/mealmate-service/commit/258567360b7ea867f02bbc58e21afba77f5b312a))
* **deps-dev:** bump com.diffplug.spotless:spotless-maven-plugin ([a82d749](https://github.com/Yggdrasil-Labs/mealmate-service/commit/a82d749b41927b077085540b0a388a377e4c566d))
* **deps-dev:** bump com.diffplug.spotless:spotless-maven-plugin from 3.4.0 to 3.7.0 ([712fb6c](https://github.com/Yggdrasil-Labs/mealmate-service/commit/712fb6c798197acde563ea790863fd4ac536c263))
* **deps:** bump io.swagger.core.v3:swagger-annotations-jakarta ([88efb13](https://github.com/Yggdrasil-Labs/mealmate-service/commit/88efb13f70632b7f490817a679545c6fd0add20f))
* **deps:** bump io.swagger.core.v3:swagger-annotations-jakarta ([b13e763](https://github.com/Yggdrasil-Labs/mealmate-service/commit/b13e76382b26565f7755470c834498f0edc54018))
* **deps:** bump io.swagger.core.v3:swagger-annotations-jakarta from 2.2.49 to 2.2.51 ([b032916](https://github.com/Yggdrasil-Labs/mealmate-service/commit/b03291672626403d0aa19733952f4844f5c3f0ac))
* gitignore 增加 *.log.gz 类型匹配 ([71b36a1](https://github.com/Yggdrasil-Labs/mealmate-service/commit/71b36a1b39debe1fe18b854f968c0342eb87941f))
* 更新数据库文档和测试配置 ([5ca7d0b](https://github.com/Yggdrasil-Labs/mealmate-service/commit/5ca7d0b393f45a40e319e1c424271fdac8bbb2d4))
* 移除不再需要的 build.sh/build.ps1 ([dde8a71](https://github.com/Yggdrasil-Labs/mealmate-service/commit/dde8a71ecb8cf008c212c4feb616558f5b712865))
* 移除仓库内 project-workflow skill 文件 ([edaa5ac](https://github.com/Yggdrasil-Labs/mealmate-service/commit/edaa5ac8a18c0ccbbe027d80344fca17f4ed1874))
