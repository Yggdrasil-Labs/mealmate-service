package io.yggdrasil.labs.mealmate.app.recipe.executor;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.recipe.convertor.RecipeConvertor;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeNutritionCmd;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.service.RecipeDomainService;

@ExtendWith(MockitoExtension.class)
class UpdateRecipeNutritionCmdExeTest {

    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeConvertor recipeConvertor;

    private final RecipeDomainService recipeDomainService = new RecipeDomainService();

    @Test
    void shouldRejectUpdateNutritionWhenRecipeIsSystem() {
        UpdateRecipeNutritionCmd cmd = new UpdateRecipeNutritionCmd();
        cmd.setRecipeId(1L);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(systemRecipe(1L)));

        UpdateRecipeNutritionCmdExe cmdExe =
                new UpdateRecipeNutritionCmdExe(
                        recipeRepository, recipeDomainService, recipeConvertor);

        assertThrows(IllegalArgumentException.class, () -> cmdExe.execute(cmd));
        verify(recipeRepository).findById(1L);
        verifyNoMoreInteractions(recipeRepository);
    }

    private Recipe systemRecipe(Long id) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setSourceType(RecipeSourceType.SYSTEM);
        return recipe;
    }
}
