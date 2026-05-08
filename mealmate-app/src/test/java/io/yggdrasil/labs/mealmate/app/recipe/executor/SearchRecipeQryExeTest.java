package io.yggdrasil.labs.mealmate.app.recipe.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.recipe.assembler.RecipeAssembler;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.SearchRecipeQry;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class SearchRecipeQryExeTest {

    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeAssembler recipeAssembler;

    @Test
    void shouldSearchRecipeByKeyword() {
        SearchRecipeQry qry = new SearchRecipeQry("Pumpkin", 5);
        Recipe recipe = new Recipe();
        RecipeCO recipeCO = new RecipeCO();
        recipeCO.setName("Pumpkin Porridge");

        when(recipeRepository.searchByKeyword("Pumpkin", 5)).thenReturn(List.of(recipe));
        when(recipeAssembler.toRecipeCOList(List.of(recipe))).thenReturn(List.of(recipeCO));

        SearchRecipeQryExe qryExe = new SearchRecipeQryExe(recipeRepository, recipeAssembler);

        List<RecipeCO> result = qryExe.execute(qry);

        assertEquals(1, result.size());
        assertEquals("Pumpkin Porridge", result.get(0).getName());
        verify(recipeRepository).searchByKeyword("Pumpkin", 5);
    }
}
