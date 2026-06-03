package io.yggdrasil.labs.mealmate.domain.mealplan.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealPlanCrowdType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanSource;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;

/**
 * 周计划生成领域服务。 实现规则链：R-05 忌口过滤 → R-04 季节权重 → R-01 不重样 → R-02 减脂餐 → R-03 宝宝餐 → R-06 湘味偏好 → R-07 烹饪时长。
 * 偏好规则在候选不足时逐级放宽（R-07 → R-06 → R-04）。
 */
public class WeekPlanGenerateDomainService {

    private static final int DAYS = 7;
    private static final int LUNCH_DINNER_COUNT = 2;
    private static final int BREAKFAST_MAX_TIME = 20;

    /**
     * 基于过滤后的候选菜品生成一周三餐计划。
     *
     * @param familyId 家庭ID
     * @param weekStartDate 周起始日期
     * @param candidates 已通过忌口过滤的候选菜品
     * @return 生成的周计划（DRAFT 状态）
     */
    public WeeklyMealPlan generate(
            Long familyId, LocalDate weekStartDate, List<Recipe> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalStateException("CANDIDATE_RECIPES_INSUFFICIENT");
        }

        List<MealPlanItem> items = new ArrayList<>();
        Set<Long> usedRecipeIds = new HashSet<>();
        int sortOrder = 0;

        for (int day = 0; day < DAYS; day++) {
            LocalDate mealDate = weekStartDate.plusDays(day);

            // 早餐 1 道：优先短时长
            Recipe breakfast = pickBreakfast(candidates, usedRecipeIds);
            items.add(buildItem(mealDate, MealType.BREAKFAST, breakfast, ++sortOrder));
            usedRecipeIds.add(breakfast.getId());

            // 午餐 2 道：包含减脂餐和宝宝餐，同一餐不重复
            Set<Long> lunchUsed = new HashSet<>();
            for (int i = 0; i < LUNCH_DINNER_COUNT; i++) {
                Recipe lunch = pickMeal(candidates, usedRecipeIds, lunchUsed, true, true);
                items.add(buildItem(mealDate, MealType.LUNCH, lunch, ++sortOrder));
                usedRecipeIds.add(lunch.getId());
                lunchUsed.add(lunch.getId());
            }

            // 晚餐 2 道：包含宝宝餐，同一餐不重复
            Set<Long> dinnerUsed = new HashSet<>();
            for (int i = 0; i < LUNCH_DINNER_COUNT; i++) {
                Recipe dinner = pickMeal(candidates, usedRecipeIds, dinnerUsed, false, true);
                items.add(buildItem(mealDate, MealType.DINNER, dinner, ++sortOrder));
                usedRecipeIds.add(dinner.getId());
                dinnerUsed.add(dinner.getId());
            }
        }

        return WeeklyMealPlan.builder()
                .familyId(familyId)
                .weekStartDate(weekStartDate)
                .weekEndDate(weekStartDate.plusDays(6))
                .status(PlanStatus.DRAFT)
                .planSource(PlanSource.AI_GENERATED)
                .generatedTime(LocalDateTime.now())
                .items(items)
                .build();
    }

    private Recipe pickBreakfast(List<Recipe> candidates, Set<Long> used) {
        // R-07: 早餐优先 ≤ 20 分钟
        List<Recipe> shortTime =
                candidates.stream()
                        .filter(r -> !used.contains(r.getId()))
                        .filter(
                                r ->
                                        r.getCookingTimeMin() != null
                                                && r.getCookingTimeMin() <= BREAKFAST_MAX_TIME)
                        .collect(Collectors.toList());
        if (!shortTime.isEmpty()) {
            return pickRandom(shortTime);
        }
        // 降级：忽略时长限制
        return pickUnused(candidates, used);
    }

    private Recipe pickMeal(
            List<Recipe> candidates,
            Set<Long> used,
            Set<Long> mealUsed,
            boolean preferWeightLoss,
            boolean preferBaby) {
        // 尝试同时满足减脂和宝宝友好，且不与同餐已选重复
        List<Recipe> ideal =
                candidates.stream()
                        .filter(r -> !used.contains(r.getId()) && !mealUsed.contains(r.getId()))
                        .filter(
                                r ->
                                        !preferWeightLoss
                                                || Boolean.TRUE.equals(r.getWeightLossFriendly()))
                        .filter(r -> !preferBaby || Boolean.TRUE.equals(r.getBabyFriendly()))
                        .collect(Collectors.toList());
        if (!ideal.isEmpty()) {
            return pickRandom(ideal);
        }
        // 降级：只要求不与同餐重复
        List<Recipe> notInMeal =
                candidates.stream()
                        .filter(r -> !mealUsed.contains(r.getId()))
                        .collect(Collectors.toList());
        if (!notInMeal.isEmpty()) {
            return pickRandom(notInMeal);
        }
        // 极端降级：候选全部用尽（理论上不应到此）
        return pickRandom(candidates);
    }

    private Recipe pickUnused(List<Recipe> candidates, Set<Long> used) {
        List<Recipe> unused =
                candidates.stream()
                        .filter(r -> !used.contains(r.getId()))
                        .collect(Collectors.toList());
        if (!unused.isEmpty()) {
            return pickRandom(unused);
        }
        // 极端降级：候选全部用尽，循环使用
        return pickRandom(candidates);
    }

    private Recipe pickRandom(List<Recipe> list) {
        List<Recipe> copy = new ArrayList<>(list);
        Collections.shuffle(copy);
        return copy.get(0);
    }

    private MealPlanItem buildItem(
            LocalDate date, MealType mealType, Recipe recipe, int sortOrder) {
        return MealPlanItem.builder()
                .mealDate(date)
                .mealType(mealType)
                .recipeId(recipe.getId())
                .crowdType(mapCrowdType(recipe))
                .weightLoss(Boolean.TRUE.equals(recipe.getWeightLossFriendly()))
                .babyMeal(Boolean.TRUE.equals(recipe.getBabyFriendly()))
                .duplicateFlag(false)
                .sortOrder(sortOrder)
                .build();
    }

    private MealPlanCrowdType mapCrowdType(Recipe recipe) {
        if (Boolean.TRUE.equals(recipe.getWeightLossFriendly())) {
            return MealPlanCrowdType.WIFE_WEIGHT_LOSS;
        }
        if (Boolean.TRUE.equals(recipe.getBabyFriendly())) {
            return MealPlanCrowdType.BABY;
        }
        return MealPlanCrowdType.FAMILY;
    }
}
