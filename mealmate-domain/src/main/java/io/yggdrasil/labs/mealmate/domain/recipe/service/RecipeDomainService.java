package io.yggdrasil.labs.mealmate.domain.recipe.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;

/** UC2 菜谱相关的领域规则与规范化逻辑。 */
public class RecipeDomainService {

    /** 规范化菜谱列表字段与子对象排序，并校验领域不变式。 */
    public Recipe normalizeRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }

        recipe.setTasteTags(normalizeTasteTags(recipe.getTasteTags()));
        recipe.setIngredients(normalizeIngredients(recipe.getIngredients()));
        recipe.setSteps(normalizeSteps(recipe.getSteps()));
        applyBabyFriendlyRule(recipe);
        if (recipe.getNutritionFact() != null) {
            validateNutritionFact(recipe.getNutritionFact());
        }
        return recipe;
    }

    /** 规范化口味标签列表。 */
    public List<String> normalizeTasteTags(List<String> tasteTags) {
        return normalizeStringList(tasteTags);
    }

    /** 根据适用人群自动补齐宝宝友好标记。 */
    public void applyBabyFriendlyRule(Recipe recipe) {
        if (recipe != null && recipe.getCrowdTag() == CrowdTag.BABY) {
            recipe.setBabyFriendly(Boolean.TRUE);
        }
    }

    /** 校验菜谱可编辑。 */
    public void assertRecipeEditable(Recipe recipe) {
        assertNotSystemRecipe(recipe, "Recipe cannot be edited");
    }

    /** 校验菜谱可删除。 */
    public void assertRecipeDeletable(Recipe recipe) {
        assertNotSystemRecipe(recipe, "Recipe cannot be deleted");
    }

    /** 校验营养值非负。 */
    public void validateNutritionFact(NutritionFact nutritionFact) {
        if (nutritionFact == null) {
            return;
        }
        if (isNegative(nutritionFact.getCalories())
                || isNegative(nutritionFact.getProtein())
                || isNegative(nutritionFact.getFat())
                || isNegative(nutritionFact.getCarbohydrate())
                || isNegative(nutritionFact.getFiber())
                || isNegative(nutritionFact.getCalcium())
                || isNegative(nutritionFact.getSodium())) {
            throw new IllegalArgumentException("Nutrition values cannot be negative");
        }
    }

    private void assertNotSystemRecipe(Recipe recipe, String message) {
        if (recipe == null
                || recipe.getSourceType() == null
                || recipe.getSourceType() == RecipeSourceType.SYSTEM) {
            throw new IllegalArgumentException(message);
        }
    }

    private List<String> normalizeStringList(List<String> values) {
        if (values == null) {
            return Collections.emptyList();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<RecipeIngredient> normalizeIngredients(List<RecipeIngredient> ingredients) {
        List<RecipeIngredient> normalizedIngredients = normalizeChildList(ingredients);
        if (normalizedIngredients.isEmpty()) {
            throw new IllegalArgumentException("Recipe ingredients cannot be empty");
        }
        for (int i = 0; i < normalizedIngredients.size(); i++) {
            normalizedIngredients.get(i).setSortNo(i + 1);
        }
        return normalizedIngredients;
    }

    public List<RecipeStep> normalizeSteps(List<RecipeStep> steps) {
        List<RecipeStep> normalizedSteps = normalizeChildList(steps);
        for (int i = 0; i < normalizedSteps.size(); i++) {
            normalizedSteps.get(i).setStepNo(i + 1);
        }
        return normalizedSteps;
    }

    private <T> List<T> normalizeChildList(List<T> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        return values.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private boolean isNegative(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) < 0;
    }
}
