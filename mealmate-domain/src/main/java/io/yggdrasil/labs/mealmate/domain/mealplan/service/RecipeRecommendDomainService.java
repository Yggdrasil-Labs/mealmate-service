package io.yggdrasil.labs.mealmate.domain.mealplan.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;

/** 菜品推荐领域服务：排除已用菜品并限制返回数量。 */
public class RecipeRecommendDomainService {

    /**
     * 从候选列表中排除已用菜品，返回不超过 limit 条结果。
     *
     * @param candidates 候选菜品列表
     * @param usedRecipeIds 已使用的菜品 ID 集合
     * @param limit 最大返回数量
     */
    public List<Recipe> recommend(List<Recipe> candidates, Set<Long> usedRecipeIds, int limit) {
        return candidates.stream()
                .filter(r -> !usedRecipeIds.contains(r.getId()))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
