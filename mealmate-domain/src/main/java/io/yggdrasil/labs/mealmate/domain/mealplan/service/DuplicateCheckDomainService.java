package io.yggdrasil.labs.mealmate.domain.mealplan.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;

/** 重复菜品检查领域服务。 标记同一周内出现超过 1 次的菜品。 */
public class DuplicateCheckDomainService {

    /** 标记重复菜品的 duplicateFlag。 同一 recipeId 出现 > 1 次，则所有该 recipeId 的 item 标记为重复。 */
    public void markDuplicates(List<MealPlanItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Map<Long, Integer> countMap = new HashMap<>();
        for (MealPlanItem item : items) {
            countMap.merge(item.getRecipeId(), 1, Integer::sum);
        }
        for (MealPlanItem item : items) {
            item.setDuplicateFlag(countMap.getOrDefault(item.getRecipeId(), 0) > 1);
        }
    }
}
