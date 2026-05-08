package io.yggdrasil.labs.mealmate.app.recipe.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.recipe.convertor.RecipeConvertor;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeCmd;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.service.RecipeDomainService;

@ExtendWith(MockitoExtension.class)
class UpdateRecipeCmdExeTest {

    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeConvertor recipeConvertor;

    private final RecipeDomainService recipeDomainService = new RecipeDomainService();

    @Test
    void shouldRejectUpdateWhenRecipeIsSystem() {
        UpdateRecipeCmd cmd = new UpdateRecipeCmd();
        cmd.setRecipeId(1L);
        cmd.setName("Tomato Egg");

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(systemRecipe(1L)));

        UpdateRecipeCmdExe cmdExe =
                new UpdateRecipeCmdExe(recipeRepository, recipeDomainService, recipeConvertor);

        assertThrows(IllegalArgumentException.class, () -> cmdExe.execute(cmd));
        verify(recipeRepository).findById(1L);
        verifyNoMoreInteractions(recipeRepository);
    }

    @Test
    void shouldUpdateBasicsWithTrimmedNameAndNormalizedTasteTags() {
        UpdateRecipeCmd cmd = new UpdateRecipeCmd();
        cmd.setRecipeId(1L);
        cmd.setName("  Pumpkin Soup  ");

        Recipe persistedRecipe = manualRecipe(1L);
        Recipe recipe = new Recipe();
        recipe.setId(1L);
        recipe.setName("  Pumpkin Soup  ");
        recipe.setCrowdTag(CrowdTag.BABY);
        recipe.setBabyFriendly(Boolean.FALSE);
        recipe.setTasteTags(List.of(" sweet ", "sweet", "", " warm "));

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(persistedRecipe));
        when(recipeRepository.findByName("Pumpkin Soup")).thenReturn(Optional.of(persistedRecipe));
        when(recipeConvertor.toRecipe(cmd)).thenReturn(recipe);

        UpdateRecipeCmdExe cmdExe =
                new UpdateRecipeCmdExe(recipeRepository, recipeDomainService, recipeConvertor);

        cmdExe.execute(cmd);

        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        verify(recipeRepository).update(recipeCaptor.capture());
        Recipe updatedRecipe = recipeCaptor.getValue();
        assertEquals("Pumpkin Soup", updatedRecipe.getName());
        assertEquals(List.of("sweet", "warm"), updatedRecipe.getTasteTags());
        assertEquals(Boolean.TRUE, updatedRecipe.getBabyFriendly());
        assertEquals(RecipeSourceType.MANUAL, updatedRecipe.getSourceType());
        assertEquals(RecipeStatus.ACTIVE, updatedRecipe.getStatus());
    }

    private Recipe systemRecipe(Long id) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setSourceType(RecipeSourceType.SYSTEM);
        return recipe;
    }

    private Recipe manualRecipe(Long id) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setSourceType(RecipeSourceType.MANUAL);
        recipe.setStatus(RecipeStatus.ACTIVE);
        return recipe;
    }
}
