# AI 流式输出（SSE）

**Status:** spec
**Created:** 2026-07-10
**Owner:** yangyang
**Resolved Path:** docs/active/llm-integration/

## Overview

将 AI 菜品解析和 AI 周计划生成的响应从同步阻塞模式改为 SSE（Server-Sent Events）流式输出。用户发送请求后，前端即时展示 AI 逐步生成的文字内容（打字机效果），消除 3-8 秒等待焦虑。流式完成后，后端发送结构化最终结果事件，前端完成业务状态更新。

---

## Behavior: AI 菜品解析流式对话

### Scenario: 正常流式解析菜品

Given 用户已登录，打开 AI 录入抽屉
When 用户输入"番茄炒蛋，2个番茄3个鸡蛋，10分钟"并发送
Then 前端建立 SSE 连接
And 在 500ms 内收到首个 `chunk` 类型事件（首字节延迟 ≤ 500ms）
And AI 回复文字逐 `chunk` 事件追加显示（打字机效果）
And 流式结束后收到事件类型为 `done`、data 为 `[DONE]` 的 SSE 事件
And 紧接着收到 `result` 类型事件，data 为完整的解析结果 JSON（sessionId、status、parsed、suggestions）
And 前端更新解析预览卡片和状态

### Scenario: 多轮对话中的流式响应

Given 用户已在会话中完成首次解析，status 为 REFINING
When 用户继续输入"先炒鸡蛋再加番茄翻炒"
Then 前端再次建立 SSE 连接（带 sessionId）
And AI 回复以流式方式逐步展示（`chunk` 事件）
And 流式结束后收到 `done` 事件和 `result` 事件
And `result` 事件中包含 merge 后的完整 parsed 数据
And status 正确反映累积后的最新状态

### Scenario: 流式过程中用户取消

Given AI 正在流式输出回复
When 用户点击"停止生成"按钮或关闭抽屉
Then 前端关闭 SSE 连接
And 已接收的部分文字保留显示
And 本轮对话的 parsed 结果不更新（未收到完整 result 事件）
And 会话状态不变，用户可重新发送消息

---

## Behavior: AI 周计划生成流式输出

### Scenario: 正常流式生成周计划

Given 用户已登录，familyId=1，菜品库有 ≥30 道菜品
When 用户点击"AI 生成"并输入"这周想吃清淡的"
Then 前端建立 SSE 连接
And 在 500ms 内收到首个 `chunk` 类型事件
And AI 的推理过程/生成说明逐步展示（`chunk` 事件）
And 流式结束后收到 `done` 事件（data 为 `[DONE]`）和 `result` 事件
And `result` 事件 data 为完整周计划 JSON（planId、reasoning、fallback=false）
And 前端刷新计划展示

### Scenario: 流式过程中 LLM 超时后 fallback

Given 用户请求 AI 生成周计划
When LLM 流式响应在中途断开（网络超时或服务端错误）
Then 后端检测到流式失败
And 发送一个 `error` 事件通知前端"AI 生成中断，正在使用规则引擎"
And 后端 fallback 到规则引擎生成计划
And 发送 `result` 事件，包含规则引擎的计划结果（fallback=true）
And 前端展示 fallback 提示 + 计划结果

---

## Behavior: SSE 连接生命周期管理

### Scenario: 正常连接建立与关闭

Given 用户发送 AI 请求
When 后端开始流式输出
Then SSE 连接建立，Content-Type 为 `text/event-stream`
And 每个文字片段通过事件类型 `chunk` 发送，data 为纯文本
And 流式完成时发送事件类型 `done`、data 为 `[DONE]`
And 紧随发送事件类型 `result`、data 为完整 JSON 结果
And 连接自动关闭

### Scenario: 连接超时

Given SSE 连接已建立
When 后端在 30 秒内未发送任何 chunk（LLM 完全无响应）
Then 后端发送 `error` 事件（包含超时错误信息）
And 对于菜品解析：连接关闭，前端提示重试
And 对于周计划生成：触发 fallback 流程

### Scenario: 客户端网络断开

Given SSE 流式输出进行中
When 客户端网络中断（前端检测到 EventSource 的 onerror）
Then 前端不自动重连（AI 流式请求不适合重连）
And 展示网络错误提示
And 用户可手动重新发送

### Scenario: 并发请求同一会话

Given 用户在 AI 菜品解析会话中
When 用户在上一条消息流式未完成时发送新消息
Then 前端拦截：上一条流式未完成时输入框 disabled
And 提示"请等待 AI 回复完成"
And 若前端拦截失败、同一 sessionId 并发到达后端，后端返回 `error` 事件（code=AI_SESSION_BUSY, message="会话正在处理中，请稍后"）

---

## Behavior: 输出截断处理

### Scenario: LLM 因 max_tokens 限制截断输出

Given 用户发送较长的菜品描述
When LLM 返回 finish_reason=length（token 用尽，输出被截断）
Then 对于菜品解析：流式正常展示已有内容，`result` 事件中 parsed 基于截断内容解析（可能不完整），status 为 REFINING，suggestions 中包含"内容较长，建议分步描述"
And 对于周计划生成：视为格式异常，重试 1 次后仍失败 → fallback

---

## Behavior: 向后兼容

### Scenario: 非流式端点保持不变

Given 现有的 `POST /api/ai/recipes/confirm` 端点
When 用户提交确认请求
Then 仍然使用同步 JSON 响应（非 SSE）
And 接口行为与 Phase 2 完全一致

### Scenario: 流式端点的结构化结果与非流式一致

Given AI 菜品解析流式端点
When 流式完成后发送 `result` 事件
Then result 的 JSON 结构与现有 `POST /api/ai/recipes/chat` 的响应体完全一致
And 前端可复用现有解析逻辑

---

## Behavior: 错误处理

### Scenario: AI 服务完全不可用

Given 用户发送 AI 请求
When DeepSeek API 返回 500/401/429（在流式建立之前）
Then 后端通过 SSE `error` 事件返回错误信息
And 前端显示对应错误提示
And 对于周计划：触发 fallback

### Scenario: AI 返回的最终 JSON 解析失败

Given AI 流式输出已完成（content 全部到达）
When 后端尝试解析 AI 的完整输出为结构化数据失败
Then 对于菜品解析：`result` 事件中 parsed 为 null，reply 保留 AI 原文，status 不变
And 对于周计划：重试 1 次后仍失败 → fallback

---

## Constraints

- 首字节延迟（TTFB）P95 ≤ 500ms（从请求到首个 chunk 事件）。度量方式：后端结构化日志记录 `streamFirstChunkLatency` 字段；集成测试中断言首个 chunk 到达时间
- 单次流式请求最大持续时间：60 秒（SseEmitter timeout，大于 DeepSeek 最大响应时长）
- SSE 事件类型固定 4 种：`chunk`、`done`、`result`、`error`
- chunk 事件的 data 为纯文本片段（UTF-8），不含 JSON 包装
- result 事件的 data 为完整 JSON 对象
- error 事件的 data 为 JSON `{ "code": "...", "message": "..." }`
- SSE 连接不支持自动重连（前端不设置 EventSource retry，使用 fetch stream）
- 确认入库（confirm）、规则引擎生成等非 AI 直接调用的接口不改为流式
- 不引入 Spring WebFlux 全栈依赖——使用 Spring WebMVC 的 SseEmitter
- 流式输出不改变业务逻辑（状态机、merge、fallback 行为完全保留）
- 同一 sessionId 同一时刻仅允许一个流式请求在处理，并发到达时返回错误
