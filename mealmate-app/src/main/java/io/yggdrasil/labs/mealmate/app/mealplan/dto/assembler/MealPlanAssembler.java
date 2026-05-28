package io.yggdrasil.labs.mealmate.app.mealplan.dto.assembler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.ConfirmPlanCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.DayMealCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.MealPlanItemCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.PrepPlanCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.PrepPlanItemCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.ShoppingItemCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.WeeklyMealPlanCO;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.ShoppingItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;

/** 周餐计划领域对象到 CO 的转换工具类。 */
public final class MealPlanAssembler {

    private MealPlanAssembler() {}

    /** 将周计划聚合根转换为 CO，按日期+餐次分组。 */
    public static WeeklyMealPlanCO toWeeklyMealPlanCO(
            WeeklyMealPlan plan, Map<Long, Recipe> recipeMap) {
        Map<String, DayMealCO> dayMeals = new LinkedHashMap<>();
        if (plan.getItems() != null) {
            // 按日期分组
            Map<String, List<MealPlanItem>> byDate =
                    plan.getItems().stream()
                            .collect(
                                    Collectors.groupingBy(
                                            item -> item.getMealDate().toString(),
                                            LinkedHashMap::new,
                                            Collectors.toList()));
            byDate.forEach(
                    (date, items) -> {
                        List<MealPlanItemCO> breakfast = new ArrayList<>();
                        List<MealPlanItemCO> lunch = new ArrayList<>();
                        List<MealPlanItemCO> dinner = new ArrayList<>();
                        for (MealPlanItem item : items) {
                            MealPlanItemCO co = toItemCO(item, recipeMap);
                            if (item.getMealType() == MealType.BREAKFAST) {
                                breakfast.add(co);
                            } else if (item.getMealType() == MealType.LUNCH) {
                                lunch.add(co);
                            } else {
                                dinner.add(co);
                            }
                        }
                        dayMeals.put(
                                date,
                                DayMealCO.builder()
                                        .date(date)
                                        .breakfast(breakfast)
                                        .lunch(lunch)
                                        .dinner(dinner)
                                        .build());
                    });
        }
        return WeeklyMealPlanCO.builder()
                .planId(plan.getId())
                .weekStartDate(
                        plan.getWeekStartDate() != null ? plan.getWeekStartDate().toString() : null)
                .weekEndDate(
                        plan.getWeekEndDate() != null ? plan.getWeekEndDate().toString() : null)
                .status(plan.getStatus() != null ? plan.getStatus().getCode() : null)
                .planSource(plan.getPlanSource() != null ? plan.getPlanSource().name() : null)
                .dayMeals(dayMeals)
                .build();
    }

    public static ConfirmPlanCO toConfirmPlanCO(
            Long planId, String status, Long prepPlanId, int prepItemCount, int shoppingItemCount) {
        return ConfirmPlanCO.builder()
                .planId(planId)
                .status(status)
                .prepPlanId(prepPlanId)
                .prepItemCount(prepItemCount)
                .shoppingItemCount(shoppingItemCount)
                .build();
    }

    public static PrepPlanCO toPrepPlanCO(PrepPlan plan) {
        if (plan == null) {
            return null;
        }
        List<PrepPlanItemCO> items =
                plan.getItems() == null
                        ? Collections.emptyList()
                        : plan.getItems().stream()
                                .map(
                                        i ->
                                                PrepPlanItemCO.builder()
                                                        .id(i.getId())
                                                        .ingredientName(i.getIngredientName())
                                                        .quantity(i.getQuantity())
                                                        .unit(i.getUnit())
                                                        .storageMethod(i.getStorageMethod())
                                                        .priority(
                                                                i.getPriority() != null
                                                                        ? i.getPriority().name()
                                                                        : null)
                                                        .taskStatus(
                                                                i.getTaskStatus() != null
                                                                        ? i.getTaskStatus()
                                                                                .getCode()
                                                                        : null)
                                                        .build())
                                .collect(Collectors.toList());
        return PrepPlanCO.builder()
                .id(plan.getId())
                .planId(plan.getPlanId())
                .pushStatus(plan.getPushStatus() != null ? plan.getPushStatus().name() : null)
                .items(items)
                .build();
    }

    public static List<ShoppingItemCO> toShoppingItemCOs(List<ShoppingItem> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(
                        i ->
                                ShoppingItemCO.builder()
                                        .id(i.getId())
                                        .ingredientName(i.getIngredientName())
                                        .totalQuantity(i.getTotalQuantity())
                                        .unit(i.getUnit())
                                        .purchased(i.isPurchased())
                                        .sortNo(i.getSortNo())
                                        .build())
                .collect(Collectors.toList());
    }

    private static MealPlanItemCO toItemCO(MealPlanItem item, Map<Long, Recipe> recipeMap) {
        Recipe recipe = recipeMap.get(item.getRecipeId());
        return MealPlanItemCO.builder()
                .itemId(item.getId())
                .recipeId(item.getRecipeId())
                .recipeName(recipe != null ? recipe.getName() : null)
                .crowdType(item.getCrowdType() != null ? item.getCrowdType().name() : null)
                .isWeightLoss(item.isWeightLoss())
                .isBabyMeal(item.isBabyMeal())
                .duplicateFlag(item.isDuplicateFlag())
                .coverImageUrl(recipe != null ? recipe.getCoverImageUrl() : null)
                .cookingTimeMin(recipe != null ? recipe.getCookingTimeMin() : null)
                .sortOrder(item.getSortOrder())
                .build();
    }
}
