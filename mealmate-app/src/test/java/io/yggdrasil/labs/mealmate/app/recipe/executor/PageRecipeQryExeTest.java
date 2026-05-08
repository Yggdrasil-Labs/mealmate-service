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
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.PageRecipeQry;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class PageRecipeQryExeTest {

    @Mock private RecipeRepository recipeRepository;
    @Mock private RecipeAssembler recipeAssembler;

    @Test
    void shouldPageRecipesWithAllFilters() {
        PageRecipeQry qry =
                new PageRecipeQry(
                        "Soup",
                        RecipeType.SOUP,
                        SeasonTag.WINTER,
                        CrowdTag.BABY,
                        true,
                        false,
                        DifficultyLevel.EASY,
                        30,
                        2,
                        20);
        Recipe recipe = new Recipe();
        RecipeCO recipeCO = new RecipeCO();
        recipeCO.setName("Winter Soup");

        when(recipeRepository.page(
                        "Soup",
                        RecipeType.SOUP,
                        SeasonTag.WINTER,
                        CrowdTag.BABY,
                        true,
                        false,
                        DifficultyLevel.EASY,
                        30,
                        2,
                        20))
                .thenReturn(List.of(recipe));
        when(recipeAssembler.toRecipeCOList(List.of(recipe))).thenReturn(List.of(recipeCO));

        PageRecipeQryExe qryExe = new PageRecipeQryExe(recipeRepository, recipeAssembler);

        List<RecipeCO> result = qryExe.execute(qry);

        assertEquals(1, result.size());
        assertEquals("Winter Soup", result.get(0).getName());
        verify(recipeRepository)
                .page(
                        "Soup",
                        RecipeType.SOUP,
                        SeasonTag.WINTER,
                        CrowdTag.BABY,
                        true,
                        false,
                        DifficultyLevel.EASY,
                        30,
                        2,
                        20);
    }
}
