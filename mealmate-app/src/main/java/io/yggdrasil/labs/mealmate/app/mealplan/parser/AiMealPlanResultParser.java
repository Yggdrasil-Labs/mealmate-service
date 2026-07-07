package io.yggdrasil.labs.mealmate.app.mealplan.parser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.app.mealplan.context.MealPlanContext;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealPlanCrowdType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import lombok.RequiredArgsConstructor;

/**
 * 解析 LLM 输出的配餐 JSON 并执行校验修正。
 *
 * <p>主要职责：
 *
 * <ul>
 *   <li>将 JSON 反序列化为 {@link AiMealPlanRawOutput}
 *   <li>校验每个 recipeId 是否在候选池内，无效则替换
 *   <li>确保每天 breakfast=1, lunch=2, dinner=2 的数量约束
 *   <li>早餐替换时优先选择 cookingTimeMin ≤ 20 的菜品
 *   <li>同一餐内不允许重复 recipeId
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class AiMealPlanResultParser {

    /** 早餐快速菜品阈值（分钟）。 */
    private static final int QUICK_COOK_THRESHOLD = 20;

    /** 每个餐次的期望数量。 */
    private static final int BREAKFAST_COUNT = 1;

    private static final int LUNCH_COUNT = 2;
    private static final int DINNER_COUNT = 2;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ObjectMapper objectMapper;

    /**
     * 解析 LLM 输出并校验修正。
     *
     * @param llmOutput LLM 返回的 JSON 字符串
     * @param context 配餐上下文（含候选菜品池）
     * @param familyId 家庭 ID（预留，暂未使用）
     * @param weekStartDate 周计划起始日期
     * @return 校验修正后的配餐结果
     * @throws AiMealPlanParseException JSON 反序列化失败时抛出
     */
    public ParsedMealPlanResult parse(
            String llmOutput, MealPlanContext context, Long familyId, LocalDate weekStartDate) {

        // 1. JSON 反序列化
        AiMealPlanRawOutput raw = deserialize(llmOutput);

        // 2. 构建有效 ID 集合和候选菜品 Map
        Set<Long> validIds = new HashSet<>(context.getCandidateIds());
        Map<Long, Recipe> recipeMap =
                context.getCandidateRecipes().stream()
                        .collect(Collectors.toMap(Recipe::getId, Function.identity()));

        // 3. 按日期索引 raw days
        Map<String, AiMealPlanRawOutput.DayPlan> dayPlanMap = buildDayPlanMap(raw);

        // 4. 遍历 7 天，逐餐次校验修正
        List<MealPlanItem> allItems = new ArrayList<>();
        Map<String, String> reasoningMap = new HashMap<>();
        // 记录已使用的 recipeId，用于全局去重参考（跨天不强制，但替换时优先选未使用的）
        Set<Long> globalUsed = new HashSet<>();

        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate date = weekStartDate.plusDays(dayOffset);
            String dateStr = date.format(DATE_FMT);

            AiMealPlanRawOutput.DayPlan dayPlan = dayPlanMap.get(dateStr);

            // 处理三个餐次
            List<MealPlanItem> breakfastItems =
                    processMeal(
                            date,
                            MealType.BREAKFAST,
                            BREAKFAST_COUNT,
                            dayPlan != null ? dayPlan.getBreakfast() : null,
                            validIds,
                            recipeMap,
                            globalUsed,
                            true);
            List<MealPlanItem> lunchItems =
                    processMeal(
                            date,
                            MealType.LUNCH,
                            LUNCH_COUNT,
                            dayPlan != null ? dayPlan.getLunch() : null,
                            validIds,
                            recipeMap,
                            globalUsed,
                            false);
            List<MealPlanItem> dinnerItems =
                    processMeal(
                            date,
                            MealType.DINNER,
                            DINNER_COUNT,
                            dayPlan != null ? dayPlan.getDinner() : null,
                            validIds,
                            recipeMap,
                            globalUsed,
                            false);

            allItems.addAll(breakfastItems);
            allItems.addAll(lunchItems);
            allItems.addAll(dinnerItems);

            // 提取 reasoning
            String reasoning =
                    (dayPlan != null && dayPlan.getReasoning() != null)
                            ? dayPlan.getReasoning()
                            : "";
            reasoningMap.put(dateStr, reasoning);
        }

        return ParsedMealPlanResult.builder().items(allItems).reasoning(reasoningMap).build();
    }

    /** 反序列化 JSON，失败时抛出 {@link AiMealPlanParseException}。 */
    private AiMealPlanRawOutput deserialize(String json) {
        try {
            return objectMapper.readValue(json, AiMealPlanRawOutput.class);
        } catch (JsonProcessingException e) {
            throw new AiMealPlanParseException("Failed to parse AI meal plan output", e);
        }
    }

    /** 将 raw days 列表按日期字符串索引。 */
    private Map<String, AiMealPlanRawOutput.DayPlan> buildDayPlanMap(AiMealPlanRawOutput raw) {
        if (raw.getDays() == null) {
            return Collections.emptyMap();
        }
        Map<String, AiMealPlanRawOutput.DayPlan> map = new HashMap<>();
        for (AiMealPlanRawOutput.DayPlan dp : raw.getDays()) {
            if (dp.getDate() != null) {
                map.put(dp.getDate(), dp);
            }
        }
        return map;
    }

    /**
     * 处理单个餐次：截取、校验、替换、补齐。
     *
     * @param date 日期
     * @param mealType 餐次类型
     * @param expectedCount 期望菜品数量
     * @param rawItems LLM 输出的原始条目（可能为 null）
     * @param validIds 有效候选 ID 集合
     * @param recipeMap 候选菜品 Map
     * @param globalUsed 全局已使用 ID（更新）
     * @param preferQuick 是否优先选择短时长菜品（早餐）
     * @return 该餐次最终的 MealPlanItem 列表
     */
    private List<MealPlanItem> processMeal(
            LocalDate date,
            MealType mealType,
            int expectedCount,
            List<AiMealPlanRawOutput.MealItem> rawItems,
            Set<Long> validIds,
            Map<Long, Recipe> recipeMap,
            Set<Long> globalUsed,
            boolean preferQuick) {

        List<MealPlanItem> result = new ArrayList<>();
        // 同一餐内已使用的 recipeId，防止餐内重复
        Set<Long> mealUsed = new HashSet<>();

        // 截取前 expectedCount 个
        List<AiMealPlanRawOutput.MealItem> truncated = truncateItems(rawItems, expectedCount);

        // 逐个校验
        for (AiMealPlanRawOutput.MealItem item : truncated) {
            Long recipeId = item.getRecipeId();

            if (recipeId != null && validIds.contains(recipeId) && !mealUsed.contains(recipeId)) {
                // 有效且不重复
                mealUsed.add(recipeId);
                globalUsed.add(recipeId);
                result.add(buildItem(date, mealType, recipeId, recipeMap, result.size()));
            } else {
                // 无效或重复 → 替换
                Long replacement = findReplacement(validIds, recipeMap, mealUsed, preferQuick);
                if (replacement != null) {
                    mealUsed.add(replacement);
                    globalUsed.add(replacement);
                    result.add(buildItem(date, mealType, replacement, recipeMap, result.size()));
                }
            }
        }

        // 不够数量时从候选池补齐
        while (result.size() < expectedCount) {
            Long filler = findReplacement(validIds, recipeMap, mealUsed, preferQuick);
            if (filler != null) {
                mealUsed.add(filler);
                globalUsed.add(filler);
                result.add(buildItem(date, mealType, filler, recipeMap, result.size()));
            } else {
                // 候选池耗尽，无法继续填充 → 跳出（极端情况）
                break;
            }
        }

        return result;
    }

    /** 截取前 N 个原始条目。 */
    private List<AiMealPlanRawOutput.MealItem> truncateItems(
            List<AiMealPlanRawOutput.MealItem> rawItems, int maxCount) {
        if (rawItems == null || rawItems.isEmpty()) {
            return Collections.emptyList();
        }
        if (rawItems.size() <= maxCount) {
            return rawItems;
        }
        return rawItems.subList(0, maxCount);
    }

    /** 从候选池中找一个替换菜品。 早餐优先选 cookingTimeMin ≤ 20 的；否则选任意未被同一餐使用的。 */
    private Long findReplacement(
            Set<Long> validIds,
            Map<Long, Recipe> recipeMap,
            Set<Long> mealUsed,
            boolean preferQuick) {

        // 优先：早餐场景下选短时长菜品
        if (preferQuick) {
            Long quickId =
                    validIds.stream()
                            .filter(id -> !mealUsed.contains(id))
                            .filter(
                                    id -> {
                                        Recipe r = recipeMap.get(id);
                                        return r != null
                                                && r.getCookingTimeMin() != null
                                                && r.getCookingTimeMin() <= QUICK_COOK_THRESHOLD;
                                    })
                            .findFirst()
                            .orElse(null);
            if (quickId != null) {
                return quickId;
            }
        }

        // 回退：选任意未被同餐使用的候选
        return validIds.stream().filter(id -> !mealUsed.contains(id)).findFirst().orElse(null);
    }

    /** 构建 MealPlanItem。根据 Recipe 属性决定 crowdType、weightLoss、babyMeal。 */
    private MealPlanItem buildItem(
            LocalDate date,
            MealType mealType,
            Long recipeId,
            Map<Long, Recipe> recipeMap,
            int sortOrder) {

        Recipe recipe = recipeMap.get(recipeId);
        MealPlanCrowdType crowdType = determineCrowdType(recipe);
        boolean weightLoss = recipe != null && Boolean.TRUE.equals(recipe.getWeightLossFriendly());
        boolean babyMeal = recipe != null && Boolean.TRUE.equals(recipe.getBabyFriendly());

        return MealPlanItem.builder()
                .mealDate(date)
                .mealType(mealType)
                .recipeId(recipeId)
                .crowdType(crowdType)
                .weightLoss(weightLoss)
                .babyMeal(babyMeal)
                .duplicateFlag(false)
                .sortOrder(sortOrder)
                .build();
    }

    /** 根据菜品属性确定用餐人群类型。 优先级：宝宝 > 减脂 > 全家。 */
    private MealPlanCrowdType determineCrowdType(Recipe recipe) {
        if (recipe == null) {
            return MealPlanCrowdType.FAMILY;
        }
        if (Boolean.TRUE.equals(recipe.getBabyFriendly())) {
            return MealPlanCrowdType.BABY;
        }
        if (Boolean.TRUE.equals(recipe.getWeightLossFriendly())) {
            return MealPlanCrowdType.WIFE_WEIGHT_LOSS;
        }
        return MealPlanCrowdType.FAMILY;
    }
}
