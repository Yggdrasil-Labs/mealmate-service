package io.yggdrasil.labs.mealmate.app.recipe.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.recipe.assembler.RecipeAssembler;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeDetailCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.GetRecipeDetailQry;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class GetRecipeDetailQryExeTest {

    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeAssembler recipeAssembler;

    @Test
    void shouldReturnRecipeDetail() {
        GetRecipeDetailQry qry = new GetRecipeDetailQry(1L);
        Recipe recipe = new Recipe();
        RecipeDetailCO recipeDetailCO = new RecipeDetailCO();
        recipeDetailCO.setId(1L);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeAssembler.toRecipeDetailCO(recipe)).thenReturn(recipeDetailCO);

        GetRecipeDetailQryExe qryExe = new GetRecipeDetailQryExe(recipeRepository, recipeAssembler);

        RecipeDetailCO result = qryExe.execute(qry);

        assertEquals(1L, result.getId());
        verify(recipeRepository).findById(1L);
    }
}
