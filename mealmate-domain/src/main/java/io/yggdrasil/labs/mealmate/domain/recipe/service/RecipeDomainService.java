package io.yggdrasil.labs.mealmate.domain.recipe.service;

import java.util.List;

import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;

/**
 * UC2 菜谱领域服务。
 *
 * <p>规范化与校验逻辑已内聚到 {@link Recipe} 聚合根。 本服务保留向后兼容的委托方法，供现有调用方平滑迁移。
 */
public class RecipeDomainService {

    /** 规范化菜谱（委托给聚合根）。 */
    public Recipe normalizeRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe cannot be null");
        }
        recipe.normalize();
        return recipe;
    }

    public List<String> normalizeTasteTags(List<String> tasteTags) {
        return Recipe.normalizeTasteTags(tasteTags);
    }

    public void applyBabyFriendlyRule(Recipe recipe) {
        if (recipe != null && recipe.getCrowdTag() == CrowdTag.BABY) {
            recipe.setBabyFriendly(Boolean.TRUE);
        }
    }

    public void assertRecipeEditable(Recipe recipe) {
        recipe.assertEditable();
    }

    public void assertRecipeDeletable(Recipe recipe) {
        recipe.assertDeletable();
    }

    public void validateNutritionFact(NutritionFact nutritionFact) {
        Recipe.validateNutritionFact(nutritionFact);
    }

    public List<RecipeIngredient> normalizeIngredients(List<RecipeIngredient> ingredients) {
        return Recipe.normalizeIngredients(ingredients);
    }

    public List<RecipeStep> normalizeSteps(List<RecipeStep> steps) {
        return Recipe.normalizeSteps(steps);
    }
}
