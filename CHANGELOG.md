# Changelog

## [1.3.0](https://github.com/Yggdrasil-Labs/mealmate-service/compare/v1.2.0...v1.3.0) (2026-09-05)


### ✨ Features

* **adapter:** 新增 AiMealPlanController 及集成测试 ([d6648d3](https://github.com/Yggdrasil-Labs/mealmate-service/commit/d6648d3134a943fd8daac08a6612b45fb3e5b0b2))
* **adapter:** 新增 AiRecipeController 及 Controller 测试 ([9344e12](https://github.com/Yggdrasil-Labs/mealmate-service/commit/9344e1292348fb703f5cbee620f8e98c6a7584ad))
* **adapter:** 新增 SSE 流式 Controller ([18deec7](https://github.com/Yggdrasil-Labs/mealmate-service/commit/18deec7d0fd092bc3c4decb7a1259e85e70edbb8))
* **app:** AI 菜品解析改为 chat 风格输出（自然语言 + JSON 块） ([00b6fb6](https://github.com/Yggdrasil-Labs/mealmate-service/commit/00b6fb66407f93708b74ecb4fb1fa3c0c5a54945))
* **app:** 新增 AI 周计划生成 DTO 和 MealPlanContextBuilder ([6b7a141](https://github.com/Yggdrasil-Labs/mealmate-service/commit/6b7a1415cbfe4f830f20c6642981411e05d5794e))
* **app:** 新增 AI 菜品解析 DTO、PromptBuilder 与 system prompt 模板 ([f230283](https://github.com/Yggdrasil-Labs/mealmate-service/commit/f230283891662b06cc4bca835980bac79c51c74d))
* **app:** 新增 AI 菜品解析和确认 Executor 及状态机编排 ([c656e08](https://github.com/Yggdrasil-Labs/mealmate-service/commit/c656e0863c65f15a8915e05ffcd7f4f6af869c10))
* **app:** 新增 AiMealPlanGenerateCmdExe 编排及 fallback 逻辑 ([a0260ff](https://github.com/Yggdrasil-Labs/mealmate-service/commit/a0260ffe52b222303b228b7e3702f31451b10225))
* **app:** 新增 AiMealPlanResultParser 及校验修正逻辑 ([b156baa](https://github.com/Yggdrasil-Labs/mealmate-service/commit/b156baa68f16d1d5544be0bb13487658aafc784e))
* **app:** 新增 MealPlanPromptBuilder 和饮食计划 system prompt ([e1fe51e](https://github.com/Yggdrasil-Labs/mealmate-service/commit/e1fe51ec2b32db8901f3e45317dc828bbe639323))
* **app:** 新增菜品解析与周计划生成的流式执行器 ([1526e9d](https://github.com/Yggdrasil-Labs/mealmate-service/commit/1526e9d957e7a7bd4ae3d6853e4e68cfbeb29d85))
* **domain:** 新增 AI 能力 domain 层接口与值对象 ([5326270](https://github.com/Yggdrasil-Labs/mealmate-service/commit/532627092f821c254da04cf6de142441b92a9e89))
* **domain:** 新增 AI 菜品解析领域类型与接口 ([8e57e7f](https://github.com/Yggdrasil-Labs/mealmate-service/commit/8e57e7f0b6686402ffb4da3997af8c3add2658f7))
* **domain:** 新增 AiChatGateway.streamChat 接口与流式 DTO ([c9992f6](https://github.com/Yggdrasil-Labs/mealmate-service/commit/c9992f64c1f147c719055d074904b4cb3ccbff3f))
* **infra:** 优化 DeepSeek 网关 — thinking mode 控制、reasoning_content 兼容、集成测试 ([ee61d00](https://github.com/Yggdrasil-Labs/mealmate-service/commit/ee61d008938355c737c797e90ed2ee62c0699a40))
* **infra:** 实现 DeepSeek 聊天网关（RestClient + WireMock 测试） ([5feb5b8](https://github.com/Yggdrasil-Labs/mealmate-service/commit/5feb5b8a41188858b9d605184efc06e2a4d8cedf))
* **infra:** 实现 Redis AI 会话存储（Mock 单元测试） ([35bd209](https://github.com/Yggdrasil-Labs/mealmate-service/commit/35bd209d002cd666d3a1eba08c24f75559507fb9))
* **infra:** 实现 streamChat 流式网关与异步线程池配置 ([5c2b297](https://github.com/Yggdrasil-Labs/mealmate-service/commit/5c2b2979dc8f4967d94b233b595f14f2fd33610d))
* **infra:** 新增 DeepSeekStreamParser SSE 行解析器 ([2a91693](https://github.com/Yggdrasil-Labs/mealmate-service/commit/2a916938b3780d9fdbb3671392896304bb691c6d))
* **infra:** 新增 DefaultPromptSanitizer 和 RedisRecipeParseCacheRepository ([7bd7105](https://github.com/Yggdrasil-Labs/mealmate-service/commit/7bd7105ce85397cb1b937d112dba8ac8df28f55a))
* **infra:** 新增 DefaultPromptSanitizer 和 RedisRecipeParseCacheRepository ([39e2739](https://github.com/Yggdrasil-Labs/mealmate-service/commit/39e2739b2d51a743f34f4aba420b58b4962f0ab2))
* **mealplan:** Controller 新增调整/推荐/历史端点 ([8ce9674](https://github.com/Yggdrasil-Labs/mealmate-service/commit/8ce96742b62303fda7bf6db5907c01ce8b04d912))
* **mealplan:** 基础设施层实现调整历史持久化 ([54539b8](https://github.com/Yggdrasil-Labs/mealmate-service/commit/54539b84d38d9a145791165357a0b7c66253bd1c))
* **mealplan:** 实现UC3生成周计划完整后端链路 ([e5eb3c6](https://github.com/Yggdrasil-Labs/mealmate-service/commit/e5eb3c6c32a55d45030cd43dac19f4867961252b))
* **mealplan:** 实现不重样校验和推荐领域服务 ([09930a1](https://github.com/Yggdrasil-Labs/mealmate-service/commit/09930a196f365a6b3c831bdf6dc84d5689985551))
* **mealplan:** 应用层实现调整/推荐/历史三个执行器 ([55c875f](https://github.com/Yggdrasil-Labs/mealmate-service/commit/55c875fe92c5e2fcf4b29c2bccab5be300fccd19))
* **mealplan:** 添加餐次调整历史表和字段迁移脚本 ([1eee4a2](https://github.com/Yggdrasil-Labs/mealmate-service/commit/1eee4a23a778d50f6a0b0c50a049bc841a3d1535))
* **mealplan:** 领域层新增调整历史实体和行为方法 ([fa5a488](https://github.com/Yggdrasil-Labs/mealmate-service/commit/fa5a488a23c43d087a97b255c7716797648ad861))
* Phase 3 AI 智能生成饮食计划（LLM 编排 + fallback 规则引擎） ([678f4f2](https://github.com/Yggdrasil-Labs/mealmate-service/commit/678f4f29d1cb5930d1c7892dafa5d1374bc3b7df))
* Phase 4.1 SSE 流式输出（后端 Flux + 前端 EventSource） ([695cfaf](https://github.com/Yggdrasil-Labs/mealmate-service/commit/695cfafc1a9aae6b2f9f5d68358fd6df45b48017))
* **recipe:** 实现菜品管理功能 ([44a3a17](https://github.com/Yggdrasil-Labs/mealmate-service/commit/44a3a17c1e7809b5a624b3ba7cbff307e7036b71))
* **start:** 新增 DeepSeek AI 配置 ([5f0c0ce](https://github.com/Yggdrasil-Labs/mealmate-service/commit/5f0c0ce0417bcc926c1be679d8150dea54e4d24d))
* 合入 UC4 调整餐次菜品(v1.3.0) ([b9d4839](https://github.com/Yggdrasil-Labs/mealmate-service/commit/b9d4839685f06553519e4aa4f0bc4d1057276442))
* 实现UC1的领域层和仓储 ([ef78ecd](https://github.com/Yggdrasil-Labs/mealmate-service/commit/ef78ecd394bb3dc6b031a8371b9a21a6bc18ed15))
* 添加家庭成员管理API及OpenAPI文档支持 ([e3e7e99](https://github.com/Yggdrasil-Labs/mealmate-service/commit/e3e7e99906634bad9811f5b95904103373183b7f))


### 🐛 Bug Fixes

* **app:** 修正菜品解析 prompt 中 crowdTag 枚举值与 Java 枚举不一致 ([c7cfd74](https://github.com/Yggdrasil-Labs/mealmate-service/commit/c7cfd74c5bd144c33ad49c5415702425e77b4798))
* **infra:** 修复 SSE 流式响应被 AccessLogFilter 阻断的问题 ([c23a88e](https://github.com/Yggdrasil-Labs/mealmate-service/commit/c23a88e4349eed2c70dff4adfb5b818ff4f22d71))
* **mealplan:** AdjustMealItemCmd 移除 path 字段 @NotNull + GetItemHistoryQryExe 增加 planId 归属校验 ([27b5910](https://github.com/Yggdrasil-Labs/mealmate-service/commit/27b5910805c094a2eaba11faba4a5df9dcc66b2d))
* **mealplan:** getCurrentWeekPlan 加载关联计划项，修复 dayMeals 返回空 ([8e1561a](https://github.com/Yggdrasil-Labs/mealmate-service/commit/8e1561a0c7a6ae5bf818ea4377dc77e0e76d7ecc))
* **mealplan:** validateNoDuplicate 异常类型改为 IllegalArgumentException ([12cdd90](https://github.com/Yggdrasil-Labs/mealmate-service/commit/12cdd90606d5bdb2e3c41bf564e14053527998e3))
* **mealplan:** 修复 N+1 查询、新增全局异常处理、补充应用层测试、丰富聚合根行为 ([7d11975](https://github.com/Yggdrasil-Labs/mealmate-service/commit/7d119756d67d7dbb5a0fb805ff4a465b43860f88))
* **mealplan:** 同一餐次内不允许出现重复菜品 ([bd9ae90](https://github.com/Yggdrasil-Labs/mealmate-service/commit/bd9ae90fd093511bebdc1299b45b1f85a37fc6e5))
* **migration:** V6 ALTER TABLE 拆分为 H2 兼容语法 ([6c162d0](https://github.com/Yggdrasil-Labs/mealmate-service/commit/6c162d06c975f29b4428d5c4c103d22c16b302a6))
* **phase1:** 代码审查修复 — 更新 updatedAt 刷新、429 日志级别、Authorization 头归属、Redis 测试覆盖率、plan 同步 ([81c12bc](https://github.com/Yggdrasil-Labs/mealmate-service/commit/81c12bc46bd8b6d40964cf4a5049376492a3f50d))
* **recipe:** 对齐前后端枚举和校验规则 ([ab26f3b](https://github.com/Yggdrasil-Labs/mealmate-service/commit/ab26f3b4b4512a9a6b5020d1e30882de25785516))
* 异常处理器重命名避免 Bean 冲突 + ConfirmPlan 加载完整聚合根 ([7c5349b](https://github.com/Yggdrasil-Labs/mealmate-service/commit/7c5349b39d96e5bb0116e28fd05d572c3bbf7d8a))


### 📝 Documentation

* **active:** 新增 LLM 集成 roadmap、Phase 1 设计与实施计划 ([e410751](https://github.com/Yggdrasil-Labs/mealmate-service/commit/e4107515f8ae8ca7dff0b25c0e4f2098cb73432a))
* **llm-integration:** phase2 plan 全部 AC 验证通过 ([103bec3](https://github.com/Yggdrasil-Labs/mealmate-service/commit/103bec3448cd96f27fef22fc0bb095ab1dd34881))
* **llm-integration:** plan 补齐 start-execution 硬化协议字段 ([23b20ba](https://github.com/Yggdrasil-Labs/mealmate-service/commit/23b20baa833e660cabcea25c39072117cdcb8820))
* **llm-integration:** 回填 Phase 4.1 plan 所有 Task 执行状态 ([e48b0b8](https://github.com/Yggdrasil-Labs/mealmate-service/commit/e48b0b81b400f39fdffde46d76d543936ea3a113))
* **llm-integration:** 新增 Phase 3 spec/design/plan + PlanSource.RULE_ENGINE 枚举修正 ([2bff0b4](https://github.com/Yggdrasil-Labs/mealmate-service/commit/2bff0b4085f44693b8ef3f21a1fed1e0d58adedc))
* **llm-integration:** 新增 Phase 4.1 SSE 流式输出设计文档 ([ad5b66e](https://github.com/Yggdrasil-Labs/mealmate-service/commit/ad5b66e07aa695e8be9f5acc528efd5a5aa49545))
* **llm-integration:** 新增 phase2 需求规格、设计与计划文档 ([6bfc0ca](https://github.com/Yggdrasil-Labs/mealmate-service/commit/6bfc0cafdc0de5ff3d8ce4897f8ffdaa3c09190c))
* **llm-integration:** 更新 phase2 plan T6 执行状态 ([04af30f](https://github.com/Yggdrasil-Labs/mealmate-service/commit/04af30f5038e192b9a0d406cf7242046c1f04031))
* **llm-integration:** 更新 phase2 plan T7 执行状态 ([aa742ce](https://github.com/Yggdrasil-Labs/mealmate-service/commit/aa742ce42c8bc54bb0612b7ee3faa1c9811f55b7))
* **llm-integration:** 更新 phase2 plan 执行状态（T1-T5 完成） ([cfd1dc4](https://github.com/Yggdrasil-Labs/mealmate-service/commit/cfd1dc44e533a4562ccf6d47eb207970723c2b98))
* **llm-integration:** 更新 phase3 plan T1-T4 执行状态 ([e027880](https://github.com/Yggdrasil-Labs/mealmate-service/commit/e0278800bc1106982d1351d1577ee4c6b5601fdb))
* **llm-integration:** 更新 phase3 plan T5 执行状态（done） ([44c2b53](https://github.com/Yggdrasil-Labs/mealmate-service/commit/44c2b535f9e9ad025da90f1de00278bfb3191a65))
* **llm-integration:** 更新 phase3 plan T6 执行状态（done） ([0b26184](https://github.com/Yggdrasil-Labs/mealmate-service/commit/0b2618473235dabe37b446d179b497e3551c43df))
* **llm-integration:** 更新 phase3 plan T6-T7 执行状态（all done） ([c989389](https://github.com/Yggdrasil-Labs/mealmate-service/commit/c989389ebf835dafaf7025c65dd1b063be27d96d))
* **llm-integration:** 标记 Phase 3 遗留 AC 全部通过 + roadmap 前置条件完成 ([35f982a](https://github.com/Yggdrasil-Labs/mealmate-service/commit/35f982aa377683e8ef0e5a3386ff5a35ef97722c))
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
* **domain:** 周计划生成来源由 AI_GENERATED 修正为 RULE_ENGINE ([3719bbc](https://github.com/Yggdrasil-Labs/mealmate-service/commit/3719bbc957f1b5d9635730eb23982905fe6105fd))
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
* **deps-dev:** bump com.diffplug.spotless:spotless-maven-plugin ([#22](https://github.com/Yggdrasil-Labs/mealmate-service/issues/22)) ([0bf0f15](https://github.com/Yggdrasil-Labs/mealmate-service/commit/0bf0f15050181687664d6323638182dca1426bd0))
* **deps-dev:** bump com.diffplug.spotless:spotless-maven-plugin from 3.4.0 to 3.7.0 ([712fb6c](https://github.com/Yggdrasil-Labs/mealmate-service/commit/712fb6c798197acde563ea790863fd4ac536c263))
* **deps:** bump io.github.yggdrasil-labs:mimir-boot-bom ([#21](https://github.com/Yggdrasil-Labs/mealmate-service/issues/21)) ([d04c843](https://github.com/Yggdrasil-Labs/mealmate-service/commit/d04c843e4e4abda38ff1b8d4ff177d06179bcfb6))
* **deps:** bump io.github.yggdrasil-labs:mimir-boot-parent ([#20](https://github.com/Yggdrasil-Labs/mealmate-service/issues/20)) ([c06c1f3](https://github.com/Yggdrasil-Labs/mealmate-service/commit/c06c1f380a64e795726021b32eb7af0544dc93a3))
* **deps:** bump io.swagger.core.v3:swagger-annotations-jakarta ([88efb13](https://github.com/Yggdrasil-Labs/mealmate-service/commit/88efb13f70632b7f490817a679545c6fd0add20f))
* **deps:** bump io.swagger.core.v3:swagger-annotations-jakarta ([b13e763](https://github.com/Yggdrasil-Labs/mealmate-service/commit/b13e76382b26565f7755470c834498f0edc54018))
* **deps:** bump io.swagger.core.v3:swagger-annotations-jakarta ([#19](https://github.com/Yggdrasil-Labs/mealmate-service/issues/19)) ([fda29ee](https://github.com/Yggdrasil-Labs/mealmate-service/commit/fda29ee07411c64ca54ec600b259a22f88ac5289))
* **deps:** bump io.swagger.core.v3:swagger-annotations-jakarta from 2.2.49 to 2.2.51 ([b032916](https://github.com/Yggdrasil-Labs/mealmate-service/commit/b03291672626403d0aa19733952f4844f5c3f0ac))
* gitignore 增加 *.log.gz 类型匹配 ([71b36a1](https://github.com/Yggdrasil-Labs/mealmate-service/commit/71b36a1b39debe1fe18b854f968c0342eb87941f))
* 更新数据库文档和测试配置 ([5ca7d0b](https://github.com/Yggdrasil-Labs/mealmate-service/commit/5ca7d0b393f45a40e319e1c424271fdac8bbb2d4))
* 移除不再需要的 build.sh/build.ps1 ([dde8a71](https://github.com/Yggdrasil-Labs/mealmate-service/commit/dde8a71ecb8cf008c212c4feb616558f5b712865))
* 移除仓库内 project-workflow skill 文件 ([edaa5ac](https://github.com/Yggdrasil-Labs/mealmate-service/commit/edaa5ac8a18c0ccbbe027d80344fca17f4ed1874))
