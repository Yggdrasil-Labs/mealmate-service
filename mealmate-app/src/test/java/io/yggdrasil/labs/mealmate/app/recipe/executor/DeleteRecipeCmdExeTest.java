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

import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.DeleteRecipeCmd;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.service.RecipeDomainService;

@ExtendWith(MockitoExtension.class)
class DeleteRecipeCmdExeTest {

    @Mock private RecipeRepository recipeRepository;

    private final RecipeDomainService recipeDomainService = new RecipeDomainService();

    @Test
    void shouldRejectDeleteWhenRecipeIsSystem() {
        DeleteRecipeCmd cmd = new DeleteRecipeCmd(1L);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(systemRecipe(1L)));

        DeleteRecipeCmdExe cmdExe = new DeleteRecipeCmdExe(recipeRepository, recipeDomainService);

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
