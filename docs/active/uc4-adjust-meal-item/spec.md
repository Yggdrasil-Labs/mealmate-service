---
id: spec-uc4-adjust-meal-item
status: draft
owner: ""
tags: [mealplan, core]
created: 2026-06-02
updated: 2026-06-02
---

# UC4 Adjust Meal Item Spec

## Overview

用户在 DRAFT 状态的周计划中替换某餐次的菜品，系统校验不重样规则、记录调整历史，并提供推荐菜品列表辅助决策。

## Behavior: 替换餐次菜品

### Scenario: 正常替换

Given 周计划 planId=1 状态为 DRAFT，itemId=10 当前菜品为「红烧肉」(recipeId=100)，crowdType=ALL
When 用户提交替换请求 {newRecipeId: 200, adjustReason: "TASTE_CHANGE"}，且 recipeId=200「清蒸鱼」本周 ALL 餐次未使用
Then 该餐次 recipe 更新为 recipeId=200
And isManuallyAdjusted=true，adjustCount 从 0 变为 1
And 系统记录一条调整历史（oldRecipeId=100, newRecipeId=200, adjustReason="TASTE_CHANGE"）
And 相关缓存被清除

### Scenario: 连续调整同一餐次

Given itemId=10 已被调整 1 次（adjustCount=1，isManuallyAdjusted=true）
When 用户再次替换为 recipeId=300
Then adjustCount 变为 2
And history 表中该 itemId 共有 2 条记录

### Scenario: 新菜品本周同 crowd 重复

Given 本周 ALL 餐次已包含 recipeId=200「清蒸鱼」
When 用户尝试将另一个 ALL 餐次替换为 recipeId=200
Then 返回错误 RECIPE_DUPLICATE_IN_WEEK (HTTP 400)
And 数据库无任何变更

### Scenario: 不同 crowd 允许相同菜品

Given 本周 ALL 餐次已包含 recipeId=200「清蒸鱼」
When 用户将一个 BABY 餐次替换为 recipeId=200
Then 替换成功（不同 crowd_type 不视为重复）

### Scenario: 计划状态非 DRAFT

Given 周计划 planId=1 状态为 CONFIRMED
When 用户提交替换请求
Then 返回错误 MEAL_PLAN_FROZEN (HTTP 400)

### Scenario: 目标菜品不存在

Given newRecipeId=999 在菜品库中不存在
When 用户提交替换请求
Then 返回错误 RECIPE_NOT_FOUND (HTTP 404)

### Scenario: 餐次不存在

Given itemId=999 不存在
When 用户提交替换请求
Then 返回错误 MEAL_PLAN_ITEM_NOT_FOUND (HTTP 404)

### Scenario: adjustReason 省略

Given 用户提交 {newRecipeId: 200}，不传 adjustReason
When 系统处理请求
Then 替换成功，history.adjust_reason 为 null

## Behavior: 获取推荐替换菜品

### Scenario: 正常获取推荐列表

Given planId=1 状态为 DRAFT，itemId=10 的 crowdType=ALL、mealType=DINNER，本周已使用 recipeIds=[100,101,102]
When 用户请求推荐列表
Then 返回候选菜品列表，不包含 recipeId 100/101/102
And 列表按匹配度排序（季节匹配 > 偏好匹配）
And 每条包含 recipeId、name、recipeType、seasonTag、coverImageUrl、cookTimeMinutes

### Scenario: 候选菜品不足

Given 菜品库中符合 crowdType=BABY 且排除已用后仅剩 2 道
When 用户请求推荐列表
Then 返回 2 条结果（不报错，有多少返回多少）

### Scenario: 从 itemId 推导上下文

Given itemId=10 的 crowdType=ALL、mealType=DINNER
When 用户请求 GET /items/10/recommend（不传额外参数）
Then 系统自动从 itemId 推导 crowdType 和 mealType 进行筛选

## Behavior: 查询调整历史

### Scenario: 有历史记录

Given itemId=10 被调整过 3 次
When 用户查询 GET /items/10/history
Then 返回 3 条记录，每条含 historyId、oldRecipeName、newRecipeName、adjustReason、adjustedAt
And 按 adjustedAt 降序排列

### Scenario: 无历史记录

Given itemId=10 从未被调整
When 用户查询历史
Then 返回空列表 (HTTP 200)

### Scenario: 餐次不存在

Given itemId=999 不存在
When 用户查询历史
Then 返回 MEAL_PLAN_ITEM_NOT_FOUND (HTTP 404)

## Constraints

- 不重样校验范围：同 crowd_type + 同一周（week_start_date ~ week_end_date）
- adjustReason 枚举值：LACK_INGREDIENT / TASTE_CHANGE / OUTING / OTHER
- 推荐列表最大返回 20 条
- 替换接口路径为 `PUT /api/meal-plans/{planId}/items/{itemId}`，替代 UC3 的 `/replace`
- 仅 DRAFT 状态允许调整；CONFIRMED / ARCHIVED 均拒绝（MEAL_PLAN_FROZEN）
- 缓存失效粒度：按周（familyId + weekStartDate），不按单 itemId
