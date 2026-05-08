package io.yggdrasil.labs.mealmate.domain.recipe.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;

/** {@link RecipeDomainService} 的规则与规范化行为单测。 */
class RecipeDomainServiceTest {

    private final RecipeDomainService recipeDomainService = new RecipeDomainService();

    @Test
    void shouldCleanupTasteTagsByTrimAndDeduplicate() {
        Recipe recipe = new Recipe();
        recipe.setTasteTags(Arrays.asList(" mild ", "", "spicy", " mild", null, "spicy ", "  "));
        recipe.setIngredients(Collections.singletonList(new RecipeIngredient()));

        Recipe normalized = recipeDomainService.normalizeRecipe(recipe);

        assertEquals(2, normalized.getTasteTags().size());
        assertEquals("mild", normalized.getTasteTags().get(0));
        assertEquals("spicy", normalized.getTasteTags().get(1));
    }

    @Test
    void shouldRejectEmptyIngredientList() {
        Recipe recipe = new Recipe();
        recipe.setIngredients(Collections.emptyList());

        assertThrows(
                IllegalArgumentException.class, () -> recipeDomainService.normalizeRecipe(recipe));
    }

    @Test
    void shouldNormalizeIngredientSortNoStartingFromOne() {
        RecipeIngredient firstIngredient = new RecipeIngredient();
        firstIngredient.setSortNo(99);
        RecipeIngredient secondIngredient = new RecipeIngredient();
        secondIngredient.setSortNo(42);
        Recipe recipe = new Recipe();
        recipe.setIngredients(Arrays.asList(firstIngredient, secondIngredient));

        Recipe normalized = recipeDomainService.normalizeRecipe(recipe);

        assertEquals(1, normalized.getIngredients().get(0).getSortNo());
        assertEquals(2, normalized.getIngredients().get(1).getSortNo());
    }

    @Test
    void shouldNormalizeStepNoStartingFromOne() {
        RecipeStep firstStep = new RecipeStep();
        firstStep.setStepNo(8);
        RecipeStep secondStep = new RecipeStep();
        secondStep.setStepNo(12);
        Recipe recipe = new Recipe();
        recipe.setIngredients(Collections.singletonList(new RecipeIngredient()));
        recipe.setSteps(Arrays.asList(firstStep, secondStep));

        Recipe normalized = recipeDomainService.normalizeRecipe(recipe);

        assertEquals(1, normalized.getSteps().get(0).getStepNo());
        assertEquals(2, normalized.getSteps().get(1).getStepNo());
    }

    @Test
    void shouldAutoEnableBabyFriendlyWhenCrowdTagIsBaby() {
        Recipe recipe = new Recipe();
        recipe.setCrowdTag(CrowdTag.BABY);
        recipe.setBabyFriendly(false);
        recipe.setIngredients(Collections.singletonList(new RecipeIngredient()));

        Recipe normalized = recipeDomainService.normalizeRecipe(recipe);

        assertTrue(normalized.getBabyFriendly());
    }

    @Test
    void shouldRejectNegativeNutritionValues() {
        NutritionFact nutritionFact = new NutritionFact();
        nutritionFact.setCalories(BigDecimal.valueOf(-1));

        assertThrows(
                IllegalArgumentException.class,
                () -> recipeDomainService.validateNutritionFact(nutritionFact));
    }

    @Test
    void shouldRejectEditingSystemRecipe() {
        Recipe recipe = new Recipe();
        recipe.setSourceType(RecipeSourceType.SYSTEM);

        assertThrows(
                IllegalArgumentException.class,
                () -> recipeDomainService.assertRecipeEditable(recipe));
    }

    @Test
    void shouldRejectDeletingSystemRecipe() {
        Recipe recipe = new Recipe();
        recipe.setSourceType(RecipeSourceType.SYSTEM);

        assertThrows(
                IllegalArgumentException.class,
                () -> recipeDomainService.assertRecipeDeletable(recipe));
    }
}
