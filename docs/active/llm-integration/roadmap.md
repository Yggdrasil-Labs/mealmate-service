---
title: LLM 集成 Roadmap
status: planning
created: 2026-06-25
updated: 2026-06-25
owner: yangyang
tags: [llm, ai, deepseek, recipe, meal-plan]
---

# LLM 集成 Roadmap

## 目标

通过集成 DeepSeek 大模型，为 MealMate 增加两项 AI 能力：

1. **智能录入菜品**：用户用自然语言描述菜品，LLM 解析为结构化数据，支持多轮对话补全
2. **智能生成饮食计划**：基于家庭画像 + 菜品库 + 用户指令，LLM 生成有营养搭配逻辑的周计划

## 决策记录

| 决策项 | 选择 | 理由 |
|--------|------|------|
| LLM 提供商 | DeepSeek | 中文理解强，性价比高，API 兼容 OpenAI 协议 |
| SDK | RestClient（Spring 6.1 原生） | Spring AI 所有版本均需 Boot 3.4+，与项目 3.3.13 不兼容；RestClient 零额外依赖 |
| 交互模式 | 多轮对话 | 菜品信息复杂，一次难以描述完整 |
| 用户控制 | 支持额外指令 | 生成计划时可给偏好提示（如"这周想吃清淡的"） |
| 降级策略 | 保留规则引擎 | LLM 不可用时 fallback 到现有随机推荐 |
| AI 接口归属 | 挂靠已有业务领域 | AI 是技术能力非业务领域，接口按服务对象归入 Recipe/MealPlan 域 |
| 会话存储 | Redis | 多轮对话需跨请求状态，Redis 提供 TTL 自动过期 |
| Prompt 模板 | 纯文本 .txt + Java String.format 占位符 | 无额外模板引擎依赖，简单直接 |

## 阶段规划

### Phase 0: 兼容性验证 ✅ 已完成

| # | 任务 | 结果 |
|---|------|------|
| 0.1 | Spring AI 兼容性 | ❌ **不兼容**。Spring AI 1.0.x/1.1.x 需 Boot 3.4+，2.0.x 需 Boot 4.0+。项目是 Boot 3.3.13。 |
| 0.2 | DeepSeek API 连通性 | ✅ endpoint 可达，返回标准 OpenAI 协议格式 |
| 0.3 | 替代方案评估 | ✅ 使用 Spring RestClient 直调 DeepSeek OpenAI 兼容接口（零额外依赖） |

**结论：** 放弃 Spring AI，采用轻量 HTTP Client 方案。技术路线：
- 使用项目已有的 `RestClient`（Spring 6.1 原生，Boot 3.2+ 可用）直接调用 DeepSeek `/chat/completions`
- 自行封装请求/响应 DTO（OpenAI ChatCompletion 格式，约 5 个类）
- 支持 `response_format: { type: "json_object" }` 实现结构化输出
- 支持 `messages[]` 多轮对话（context 由应用层管理）
- 零新依赖引入（RestClient + Jackson 均已存在）

**优势：** 无版本冲突风险、无框架锁定、代码量可控（~200 行封装）、完全控制重试/超时/日志。

### Phase 1: 基础设施层（3-4 天）

建立 LLM 调用基础能力。无新 Maven 依赖（RestClient + Jackson 已有）。

| # | 任务 | 交付物 |
|---|------|--------|
| 1.1 | 定义 OpenAI 协议 DTO（ChatRequest, ChatResponse, Message 等） | `infrastructure/ai/deepseek/dto/` |
| 1.2 | 实现 DeepSeekChatClient（RestClient 封装 + 超时/重试/错误处理） | `infrastructure/ai/deepseek/DeepSeekChatClient.java` |
| 1.3 | DeepSeek API 配置（application.yml + 环境变量注入 API Key） | 配置类 `DeepSeekProperties` |
| 1.4 | 引入 spring-boot-starter-data-redis + 配置 | pom.xml + application.yml |
| 1.5 | 定义会话存储 port + Redis 实现 | `app` 层 `AiSessionPort` 接口 + `infrastructure/ai/session/RedisAiSessionStore` |
| 1.6 | LLM 调用可观测性（结构化日志：延迟/token/model） | 日志切面或手动埋点 |
| 1.7 | 单元测试（WireMock 模拟 DeepSeek）+ 集成测试 | 确认端到端可调通 |

**验收标准：** 能通过代码发送 prompt 给 DeepSeek 并拿到结构化 JSON 响应；Redis 会话可写入/读取/过期。

### Phase 2: AI 智能录入菜品（7-8 天）

核心用例：自然语言 → 结构化菜品 → 用户确认 → 入库。

| # | 任务 | 交付物 |
|---|------|--------|
| 2.1 | 设计 Prompt 模板（recipe-parse-system.txt） | 含 JSON Schema 约束、示例、安全边界 |
| 2.2 | 在 Recipe 域定义策略接口 `RecipeParser` | `domain/recipe/service/RecipeParser.java` |
| 2.3 | Infrastructure 层实现 `DeepSeekRecipeParser` | RestClient 调用 + JSON Output 解析 |
| 2.4 | 实现多轮对话状态机 | PARSING → REFINING → READY_TO_CONFIRM |
| 2.5 | 实现 App 层编排 `AiRecipeParseAppService` | 管理会话 + 调用 Parser + 校验 |
| 2.6 | 实现 Controller `AiRecipeController` | API 入口 |
| 2.7 | 输入安全处理（prompt injection 防御 + 输出清洗） | 敏感内容过滤 |
| 2.8 | 前端：菜品库页面增加"AI 录入"抽屉组件 | `modules/recipe/` 下新增 AI 相关 api/types |
| 2.9 | 前端：对话界面 + 解析结果预览 + 编辑确认 | 多轮交互 UI |
| 2.10 | 测试：单测 mock LLM + E2E fixture 固化响应 | AI 链路可重复验证 |

**验收标准：**
- 用户输入"番茄炒蛋，2个番茄3个鸡蛋，10分钟"→ 正确解析为 CreateRecipeCmd
- 支持追问"热量大概多少？" → LLM 补充营养估算
- 用户确认后成功入库并在菜品列表可见
- Prompt injection 输入不会泄露系统 prompt 或产生非法操作

### Phase 3: AI 智能生成饮食计划（7-8 天）

核心用例：家庭上下文 + 用户指令 → LLM 编排周计划 → 用户调整 → 确认。

| # | 任务 | 交付物 |
|---|------|--------|
| 3.1 | 设计 Prompt 模板（meal-plan-generate-system.txt） | 含家庭画像格式、菜品摘要、约束规则 |
| 3.2 | 在 MealPlan 域定义策略接口 `MealPlanAdvisor` | `domain/mealplan/service/MealPlanAdvisor.java` |
| 3.3 | Infrastructure 层实现 `DeepSeekMealPlanAdvisor` | RestClient 调用 + 解析推荐列表 |
| 3.4 | 实现 Context Builder（组装家庭画像 + 菜品库摘要 + 近期历史 + 用户指令） | App 层 |
| 3.5 | 实现 AI 生成 → 转 WeeklyMealPlan + 推荐理由 | App 层编排 |
| 3.6 | LLM 不可用时 fallback 到 `WeekPlanGenerateDomainService` | 降级路径 |
| 3.7 | 前端：周计划页面增加"AI 生成"按钮 + 指令输入框 | `modules/meal-plan/` 下新增 AI api/types |
| 3.8 | 前端：展示每日推荐理由 + 允许逐项调整 | reasoning 展示组件 |
| 3.9 | 测试：单测 + E2E（mock AI 响应） | AI 计划生成全链路 spec |

**验收标准：**
- 用户输入"这周想吃清淡的川菜" → 生成符合偏好的周计划
- 每日推荐附带理由（如"周三安排鱼因为连续两天吃肉"）
- LLM 不可用时自动 fallback 到规则引擎，用户看到提示

### Phase 4: 优化与增强（持续迭代）

| # | 任务 | 优先级 |
|---|------|--------|
| 4.1 | 流式输出 SSE（后端 Flux + 前端 EventSource/fetch stream） | P1 |
| 4.2 | Prompt 版本管理 + 效果评估体系 | P1 |
| 4.3 | 成本控制（每用户每日限额 + token 预算 + 相似查询缓存） | P1 |
| 4.4 | 分布式追踪（标记 AI 调用段） | P2 |
| 4.5 | 离线模式（Ollama + Qwen2.5 本地部署） | P2 |
| 4.6 | 语音输入 → 文字 → AI 解析 | P3 |
| 4.7 | 图片识别菜品（多模态） | P3 |

## 架构设计

### 核心原则

- **AI 是技术能力，不是业务领域**：不新增 `domain/ai/` 包，接口按服务对象归入 Recipe 或 MealPlan 域
- **依赖倒置**：domain 定义策略接口，infrastructure 提供 DeepSeek 实现
- **不侵入现有模型**：Recipe / WeeklyMealPlan 聚合根不因 AI 而改变结构
- **App 层编排**：AI 调用 + 会话管理 + 校验确认 = App 层职责

### 分层归属

```
adapter/web/
  ├── AiRecipeController.java           # AI 菜品解析 API
  └── AiMealPlanController.java         # AI 计划生成 API

app/ai/
  ├── AiRecipeParseAppService.java      # 菜品解析编排（会话 + Parser + 校验）
  ├── AiMealPlanAppService.java         # 计划生成编排（context + Advisor + fallback）
  ├── port/
  │   └── AiSessionPort.java            # 会话存储接口（App 层定义）
  └── dto/
      ├── AiChatMessageCmd.java         # { sessionId?, message }
      ├── AiChatReplyDTO.java           # { sessionId, reply, parsed?, status, suggestions }
      └── AiMealPlanGenerateCmd.java    # { familyId, weekStartDate, userHint? }

domain/recipe/service/
  └── RecipeParser.java                 # 策略接口：文本 → Recipe 结构化数据

domain/mealplan/service/
  └── MealPlanAdvisor.java              # 策略接口：context → 推荐计划 + 理由

infrastructure/ai/
  ├── deepseek/
  │   ├── DeepSeekChatClient.java       # RestClient 封装（调用 /chat/completions）
  │   ├── DeepSeekProperties.java       # API 配置（base-url, api-key, model, timeout）
  │   ├── DeepSeekRecipeParser.java     # RecipeParser 实现
  │   ├── DeepSeekMealPlanAdvisor.java  # MealPlanAdvisor 实现
  │   └── dto/                          # OpenAI 协议 DTO
  │       ├── ChatRequest.java
  │       ├── ChatResponse.java
  │       ├── ChatMessage.java
  │       └── ChatChoice.java
  ├── session/
  │   └── RedisAiSessionStore.java      # AiSessionPort 实现
  ├── prompt/
  │   ├── recipe-parse-system.txt       # 菜品解析 system prompt
  │   └── meal-plan-generate-system.txt # 计划生成 system prompt
  └── safety/
      └── PromptSanitizer.java          # 输入清洗 + injection 防御
```

### 依赖方向

```
adapter → app/ai → domain/recipe/service/RecipeParser (接口)
                 → domain/mealplan/service/MealPlanAdvisor (接口)
                 → app/ai/port/AiSessionPort (接口)

infrastructure/ai → domain/recipe (实现 RecipeParser)
                  → domain/mealplan (实现 MealPlanAdvisor)
                  → app/ai/port (实现 AiSessionPort)
                  → RestClient → DeepSeek API (https://api.deepseek.com/chat/completions)
                  → Redis
```

符合 ARCHITECTURE.md 禁止的依赖方向约束：无 adapter→domain、无 app→infrastructure、无 domain→外部。

### 前端模块归属

AI 功能不单独建 `src/modules/ai/`，而是分散到已有模块：

```
src/modules/recipe/
  ├── api.ts          # 新增 aiParseChat(), aiParseConfirm()
  ├── types.ts        # 新增 AiChatMessage, AiChatReply, AiParseStatus
  └── ...

src/modules/meal-plan/
  ├── api.ts          # 新增 aiGeneratePlan()
  ├── types.ts        # 新增 AiPlanResult, AiReasoning
  └── ...

src/composables/
  └── useAiChat.ts    # 通用多轮对话 composable（管理 sessionId、消息列表、loading）
```

### API 设计

```yaml
# UC-AI-1: 智能录入菜品（多轮对话）
POST /api/ai/recipes/chat
Request:
  { "sessionId": "uuid | null", "message": "番茄炒蛋，2个番茄3个鸡蛋" }
Response:
  {
    "sessionId": "uuid",
    "reply": "我帮你整理了菜品信息，请确认...",
    "parsed": CreateRecipeCmd | null,
    "status": "PARSING | REFINING | READY_TO_CONFIRM",
    "suggestions": ["建议补充营养信息"]
  }

POST /api/ai/recipes/confirm
Request:  { "sessionId": "uuid", "recipe": CreateRecipeCmd }
Response: { "recipeId": 123 }

# UC-AI-2: 智能生成饮食计划
POST /api/ai/meal-plans/generate
Request:
  { "familyId": 1, "weekStartDate": "2026-06-30", "userHint": "这周想吃清淡的" }
Response:
  {
    "plan": WeeklyMealPlanCO,
    "reasoning": { "2026-06-30": "...", "2026-07-01": "..." },
    "fallback": false
  }
```

> **API 风格说明：** 对话式 AI 接口采用 RPC-style（`/chat`、`/confirm`、`/generate`），不强求 RESTful 资源语义。这是有意的取舍——AI 对话本质是"操作"而非"资源 CRUD"。

### 多轮对话状态机

```
[首次输入] → PARSING
                ↓ (解析成功但信息不完整)
           REFINING ←→ [用户补充]
                ↓ (信息充分)
      READY_TO_CONFIRM → [用户预览/编辑]
                ↓ (确认)
             DONE → 调用 CreateRecipeCmdExe 入库
```

- 会话 TTL：30 分钟无活动自动过期
- 最大轮次：10 轮（防止无限对话）

### 测试策略

| 层级 | 策略 |
|------|------|
| Domain 单测 | RecipeParser / MealPlanAdvisor 接口的契约测试（mock 实现） |
| App 单测 | Mock RecipeParser + AiSessionPort，验证编排逻辑和状态机 |
| Infrastructure 单测 | WireMock 模拟 DeepSeek API 响应，验证 prompt 构建和响应解析 |
| 集成测试 | 真实 DeepSeek API（仅 CI 环境，可选跳过） |
| E2E | Mock Server 返回固化 fixture，保证测试可重复且不依赖外部 API |

## 安全考量

| 风险 | 缓解 |
|------|------|
| Prompt injection | PromptSanitizer 清洗用户输入；System prompt 与 user message 严格分离 |
| 输出安全 | LLM 输出经 JSON Schema 校验 + 值域检查，不直接信任 |
| API Key 泄露 | 环境变量注入，不进入代码/配置文件/日志 |
| 数据隐私 | 仅传递必要字段给 LLM（菜名/食材名），不传家庭成员真实姓名 |
| 滥用 | 每用户每日调用限额（Phase 4 细化） |

## 风险与缓解

| 风险 | 影响 | 缓解 |
|------|------|------|
| DeepSeek API 不可用 | 功能降级 | 规则引擎 fallback + 前端提示 |
| 解析不准确 | 用户体验差 | 预览确认 + 多轮修正 + prompt 持续优化 |
| 响应慢（3-8s） | 等待焦虑 | Phase 4 SSE 流式 + loading 动画 |
| Token 成本失控 | 运营成本 | 限额 + prompt 精简 + 缓存 |
| 会话状态丢失 | 对话中断 | Redis 持久化 + 前端 sessionId 本地保持 |

## 前置条件

- [x] DeepSeek API endpoint 可达（已验证）
- [x] 技术方案确定：RestClient 直调（已验证 Spring AI 不兼容）
- [ ] DeepSeek API Key 申请
- [ ] Redis 实例可用（本地 Docker / 云服务）
- [ ] 确认 E2E 环境 docker-compose 中需新增 Redis 容器

## 文档同步计划

完成各 Phase 后需同步更新：

- `docs/DOMAINS.md`：Recipe 域补充"支持 AI 解析录入（策略模式）"，MealPlan 域补充"支持 AI 生成推荐（策略模式）"
- `ARCHITECTURE.md`：infrastructure 层补充 AI gateway 说明
- `mealmate-web/docs/DOMAINS.md`：前端模块职责更新

## 下一步

Phase 0 已完成。下一步：
1. 申请 DeepSeek API Key
2. 编写 Phase 1 的 design.md（DeepSeekChatClient 详细设计 + DTO 协议）
3. 开始 Phase 1 实施
