# AI 智能生成饮食计划

**Status:** spec
**Created:** 2026-07-05
**Owner:** yangyang
**Resolved Path:** docs/active/llm-integration/

## Overview

用户在周计划页面点击"AI 生成"，输入本周饮食偏好指令（如"这周想吃清淡的川菜"），系统组装家庭画像、菜品库摘要和近期历史作为上下文，调用 LLM 生成一周三餐计划并附带每日推荐理由。用户可逐项调整后确认。LLM 不可用时自动 fallback 到现有规则引擎。

---

## Behavior: AI 生成周计划

### Scenario: 正常生成含用户指令的周计划

Given 用户已登录，familyId=1，菜品库中有 ≥30 道菜品
And 家庭成员包含：爸爸（减脂目标）、妈妈（正常饮食）、宝宝（1岁半）
And 成员偏好包含：忌口花生、宝宝不吃辣
When 用户请求 AI 生成周计划，weekStartDate=下周一，userHint="这周想吃清淡的川菜"
Then 返回完整的一周三餐计划（7天 × 早/午/晚）
And 每天每餐安排的菜品均来自菜品库（recipeId 有效）
And 计划遵守忌口约束（不含花生相关菜品）
And 午餐/晚餐包含适合宝宝的菜品
And 有减脂标记的菜品出现在计划中
And 返回每日推荐理由 Map（key=日期，value=理由文本）
And 理由中体现用户偏好（"清淡"、"川菜"相关描述）
And 计划状态为 DRAFT
And 计划来源为 AI_GENERATED
And fallback 标记为 false

### Scenario: 不带用户指令的默认生成

Given 用户已登录，familyId=1，菜品库中有 ≥30 道菜品
When 用户请求 AI 生成周计划，weekStartDate=下周一，userHint 为空
Then 返回完整的一周三餐计划
And 推荐理由基于家庭画像和营养搭配逻辑（非空）
And 计划来源为 AI_GENERATED

### Scenario: 菜品库不足时

Given 用户已登录，菜品库中仅有 5 道菜品
When 用户请求 AI 生成周计划
Then LLM 基于有限菜品尽可能安排
And 推荐理由中说明菜品数量有限
And 可能出现菜品重复（同一菜品在不同天出现）

### Scenario: 覆盖已有 DRAFT 计划

Given 当前周已有一份 DRAFT 状态的计划
When 用户请求 AI 重新生成同一周的计划
Then 旧 DRAFT 计划被覆盖（逻辑删除旧计划 + 删除旧 items）
And 返回新生成的计划

### Scenario: 当前周已有 CONFIRMED 计划

Given 当前周已有一份 CONFIRMED 状态的计划
When 用户请求 AI 生成同一周的计划
Then 返回错误"该周计划已确认，无法重新生成"
And 不创建新计划

---

## Behavior: LLM 不可用时 fallback

### Scenario: AI 服务超时

Given 用户已登录
When 用户请求 AI 生成周计划，但 AI 服务在 20 秒内无响应
Then 系统自动调用规则引擎生成计划
And 返回的计划不含推荐理由（reasoning 为空 Map）
And fallback 标记为 true
And 前端显示提示"AI 服务暂不可用，已使用规则引擎生成"

### Scenario: AI 服务返回错误

Given 用户已登录
When 用户请求 AI 生成周计划，但 AI 服务返回 500/429/401
Then 系统自动 fallback 到规则引擎
And fallback 标记为 true

### Scenario: AI 返回格式异常无法解析

Given 用户已登录
When AI 服务返回的内容无法解析为合法的周计划 JSON（重试 1 次后仍失败）
Then 系统 fallback 到规则引擎
And fallback 标记为 true
And 日志记录异常 AI 输出用于后续 prompt 优化

---

## Behavior: AI 输出校验与修正

### Scenario: AI 推荐的 recipeId 不在菜品库中

Given AI 返回计划中包含 recipeId=999，但该 ID 不存在于菜品库
Then 系统将该 item 替换为同餐次的随机候选菜品
And 推荐理由中对应日期标注"已自动调整"
And 不对用户暴露无效 ID 错误

### Scenario: AI 输出的餐次结构不完整

Given AI 返回的 JSON 缺少某天或某餐的数据
Then 系统为缺失的 slot 用规则引擎补齐
And 推荐理由中标注补齐的部分
And 最终返回完整的 7天 × 3餐 计划

### Scenario: AI 输出违反忌口约束

Given 家庭成员忌口"花生"
When AI 推荐了一道含花生的菜品
Then 系统在后置校验中拦截
And 替换为不含花生的候选菜品
And 推荐理由中不暴露替换细节（保持自然）

---

## Behavior: 用户调整与确认

### Scenario: 用户调整 AI 生成计划中的某一项

Given 用户收到 AI 生成的 DRAFT 计划
When 用户对某天午餐的一道菜进行调整（使用现有调整功能）
Then 该 item 被替换为新菜品
And 该 item 标记为 manuallyAdjusted=true
And 推荐理由保持不变（理由是生成时的快照）
And 计划状态仍为 DRAFT

### Scenario: 用户确认 AI 生成的计划

Given 用户收到 AI 生成的 DRAFT 计划（可能经过部分手动调整）
When 用户使用现有确认功能确认计划
Then 计划状态变为 CONFIRMED
And 派生采购清单和备菜计划（复用现有逻辑）

---

## Behavior: 上下文组装

### Scenario: 家庭画像信息完整

Given familyId=1 有 3 个成员，每人有完整偏好
When 系统组装 LLM 上下文
Then 上下文包含：每位成员的角色、年龄段、饮食目标、口味偏好、忌口/过敏
And 上下文包含：菜品库摘要（菜名 + 标签 + 适宜人群，不含详细步骤）
And 上下文包含：用户本次指令
And 上下文不包含成员真实姓名（使用角色代称：爸爸、妈妈、宝宝）
And 上下文总 token 量 ≤ 6000（prompt + context）

### Scenario: 菜品库摘要超长截断

Given 菜品库有 200 道菜品
When 系统组装上下文
Then 菜品摘要按相关度/多样性筛选，控制在 80 道以内
And 优先包含近期未使用的菜品
And 标注每道菜的标签和适宜人群

---

## Constraints

- 生成 API P95 延迟 ≤ 15 秒（含 LLM 调用 5-10 秒 + 校验/修正开销），LLM 超时 20 秒
- fallback 到规则引擎时 ≤ 3 秒
- 菜品库摘要传给 LLM 的菜品数量上限：80 道
- LLM 上下文总量 ≤ 6000 tokens（system + context + user hint）
- AI 输出必须经过 recipeId 有效性校验后才返回用户（candidateIds 已过忌口）
- 不向 LLM 传递家庭成员真实姓名，使用角色代称
- LLM 不可用时的 fallback 对用户透明（前端明确告知）
- 同一个 familyId + weekStartDate 下只允许存在一份 DRAFT（覆盖式生成）
- 计划中的 recipeId 必须全部有效（在候选池中）
- API Key 不进入代码、日志或配置文件
- 同一 familyId + weekStartDate 的并发请求需保证互斥，不产生重复计划
