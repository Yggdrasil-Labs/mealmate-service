package io.yggdrasil.labs.mealmate.app.recipe.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.recipe.assembler.RecipeAssembler;
import io.yggdrasil.labs.mealmate.app.recipe.convertor.RecipeConvertor;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.CreateRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeDetailCO;
import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.service.RecipeDomainService;

@ExtendWith(MockitoExtension.class)
class CreateRecipeCmdExeTest {

    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeConvertor recipeConvertor;
    @Mock private RecipeAssembler recipeAssembler;

    private final RecipeDomainService recipeDomainService = new RecipeDomainService();

    @Test
    void shouldRejectCreateWhenRecipeNameDuplicated() {
        CreateRecipeCmd cmd = new CreateRecipeCmd();
        cmd.setName("Tomato Egg");

        when(recipeRepository.findByName("Tomato Egg")).thenReturn(Optional.of(new Recipe()));

        CreateRecipeCmdExe cmdExe =
                new CreateRecipeCmdExe(
                        recipeRepository, recipeDomainService, recipeConvertor, recipeAssembler);

        assertThrows(IllegalArgumentException.class, () -> cmdExe.execute(cmd));
        verifyNoInteractions(recipeConvertor);
        verifyNoInteractions(recipeAssembler);
    }

    @Test
    void shouldCreateRecipeWithNormalizedChildrenAndNutrition() {
        CreateRecipeCmd cmd = new CreateRecipeCmd();
        cmd.setName("  Pumpkin Soup  ");

        Recipe recipe = new Recipe();
        recipe.setName("  Pumpkin Soup  ");
        recipe.setCrowdTag(CrowdTag.BABY);
        recipe.setBabyFriendly(Boolean.FALSE);
        recipe.setTasteTags(List.of(" sweet ", "", "sweet", " warm "));
        recipe.setIngredients(List.of(ingredient("Pumpkin", 7), ingredient("Milk", 3)));
        recipe.setSteps(List.of(step(9, "Blend"), step(4, "Steam")));
        recipe.setNutritionFact(new NutritionFact());
        recipe.getNutritionFact().setCalories(BigDecimal.valueOf(88));

        RecipeDetailCO recipeDetailCO = new RecipeDetailCO();
        recipeDetailCO.setId(100L);

        when(recipeRepository.findByName("Pumpkin Soup")).thenReturn(Optional.empty());
        when(recipeConvertor.toRecipe(cmd)).thenReturn(recipe);
        when(recipeRepository.save(any(Recipe.class)))
                .thenAnswer(
                        invocation -> {
                            Recipe saved = invocation.getArgument(0);
                            saved.setId(100L);
                            return saved;
                        });
        when(recipeAssembler.toRecipeDetailCO(any(Recipe.class))).thenReturn(recipeDetailCO);

        CreateRecipeCmdExe cmdExe =
                new CreateRecipeCmdExe(
                        recipeRepository, recipeDomainService, recipeConvertor, recipeAssembler);

        RecipeDetailCO result = cmdExe.execute(cmd);

        assertEquals(100L, result.getId());
        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).save(recipeCaptor.capture());
        Recipe savedRecipe = recipeCaptor.getValue();
        assertEquals("Pumpkin Soup", savedRecipe.getName());
        assertEquals(RecipeSourceType.MANUAL, savedRecipe.getSourceType());
        assertEquals(RecipeStatus.ACTIVE, savedRecipe.getStatus());
        assertEquals(List.of("sweet", "warm"), savedRecipe.getTasteTags());
        assertEquals(Boolean.TRUE, savedRecipe.getBabyFriendly());
        assertEquals(1, savedRecipe.getIngredients().get(0).getSortNo());
        assertEquals(2, savedRecipe.getIngredients().get(1).getSortNo());
        assertEquals(1, savedRecipe.getSteps().get(0).getStepNo());
        assertEquals(2, savedRecipe.getSteps().get(1).getStepNo());
    }

    private RecipeIngredient ingredient(String name, Integer sortNo) {
        RecipeIngredient ingredient = new RecipeIngredient();
        ingredient.setIngredientName(name);
        ingredient.setSortNo(sortNo);
        return ingredient;
    }

    private RecipeStep step(Integer stepNo, String content) {
        RecipeStep step = new RecipeStep();
        step.setStepNo(stepNo);
        step.setContent(content);
        return step;
    }
}
