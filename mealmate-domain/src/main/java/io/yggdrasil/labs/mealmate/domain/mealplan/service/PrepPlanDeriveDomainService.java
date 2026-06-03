package io.yggdrasil.labs.mealmate.domain.mealplan.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.ShoppingItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PrepPriority;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PrepTaskStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PushStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;

/** 派生备菜计划与采购清单的领域服务。 根据确认后的计划项和对应菜品食材，生成归并后的备菜任务和采购清单。 */
public class PrepPlanDeriveDomainService {

    /** 从计划项和对应菜品列表派生备菜计划。 */
    public PrepPlan derivePrepPlan(
            Long planId, List<MealPlanItem> items, Map<Long, Recipe> recipeMap) {
        Map<String, PrepPlanItem> merged = new LinkedHashMap<>();
        for (MealPlanItem item : items) {
            Recipe recipe = recipeMap.get(item.getRecipeId());
            if (recipe == null || recipe.getIngredients() == null) {
                continue;
            }
            for (RecipeIngredient ing : recipe.getIngredients()) {
                merged.merge(
                        ing.getIngredientName().toLowerCase(),
                        PrepPlanItem.builder()
                                .ingredientName(ing.getIngredientName())
                                .quantity(ing.getQuantity())
                                .unit(ing.getUnit())
                                .priority(PrepPriority.NORMAL)
                                .taskStatus(PrepTaskStatus.TODO)
                                .build(),
                        (existing, incoming) -> {
                            if (existing.getQuantity() != null && incoming.getQuantity() != null) {
                                existing.setQuantity(
                                        existing.getQuantity().add(incoming.getQuantity()));
                            }
                            return existing;
                        });
            }
        }
        return PrepPlan.builder()
                .planId(planId)
                .pushStatus(PushStatus.INIT)
                .generatedTime(LocalDateTime.now())
                .items(new ArrayList<>(merged.values()))
                .build();
    }

    /** 从计划项和对应菜品列表派生采购清单。 */
    public List<ShoppingItem> deriveShoppingList(
            Long planId, List<MealPlanItem> items, Map<Long, Recipe> recipeMap) {
        Map<String, ShoppingItem> merged = new LinkedHashMap<>();
        int sortNo = 0;
        for (MealPlanItem item : items) {
            Recipe recipe = recipeMap.get(item.getRecipeId());
            if (recipe == null || recipe.getIngredients() == null) {
                continue;
            }
            for (RecipeIngredient ing : recipe.getIngredients()) {
                String key = ing.getIngredientName().toLowerCase();
                merged.merge(
                        key,
                        ShoppingItem.builder()
                                .planId(planId)
                                .ingredientName(ing.getIngredientName())
                                .totalQuantity(ing.getQuantity())
                                .unit(ing.getUnit())
                                .purchased(false)
                                .sortNo(++sortNo)
                                .build(),
                        (existing, incoming) -> {
                            if (existing.getTotalQuantity() != null
                                    && incoming.getTotalQuantity() != null) {
                                existing.setTotalQuantity(
                                        existing.getTotalQuantity()
                                                .add(incoming.getTotalQuantity()));
                            }
                            return existing;
                        });
            }
        }
        return new ArrayList<>(merged.values());
    }
}
