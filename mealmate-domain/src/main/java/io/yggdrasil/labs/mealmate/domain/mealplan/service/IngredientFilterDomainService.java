package io.yggdrasil.labs.mealmate.domain.mealplan.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;

/** 忌口/过敏食材过滤领域服务。 根据家庭成员的忌口和过敏食材列表，过滤掉不适合的候选菜品。 */
public class IngredientFilterDomainService {

    /**
     * 过滤掉含有忌口或过敏食材的菜品。
     *
     * @param candidates 候选菜品列表
     * @param avoidIngredients 忌口食材集合
     * @param allergyIngredients 过敏食材集合
     * @return 安全的候选菜品列表
     */
    public List<Recipe> filter(
            List<Recipe> candidates, Set<String> avoidIngredients, Set<String> allergyIngredients) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> forbidden = mergeToLowerCase(avoidIngredients, allergyIngredients);
        if (forbidden.isEmpty()) {
            return candidates;
        }
        return candidates.stream()
                .filter(recipe -> !containsForbidden(recipe, forbidden))
                .collect(Collectors.toList());
    }

    private Set<String> mergeToLowerCase(Set<String> a, Set<String> b) {
        Set<String> merged =
                (a == null ? Collections.<String>emptySet() : a)
                        .stream().map(String::toLowerCase).collect(Collectors.toSet());
        if (b != null) {
            b.stream().map(String::toLowerCase).forEach(merged::add);
        }
        return merged;
    }

    private boolean containsForbidden(Recipe recipe, Set<String> forbidden) {
        if (recipe.getIngredients() == null) {
            return false;
        }
        return recipe.getIngredients().stream()
                .anyMatch(ing -> forbidden.contains(ing.getIngredientName().toLowerCase()));
    }
}
