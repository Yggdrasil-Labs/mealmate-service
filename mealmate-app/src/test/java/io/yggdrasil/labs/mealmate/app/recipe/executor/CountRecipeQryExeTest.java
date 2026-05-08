package io.yggdrasil.labs.mealmate.app.recipe.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.PageRecipeQry;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;

@ExtendWith(MockitoExtension.class)
class CountRecipeQryExeTest {

    @Mock private RecipeRepository recipeRepository;

    @Test
    void shouldCountRecipesWithAllFilters() {
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
        when(recipeRepository.count(
                        "Soup",
                        RecipeType.SOUP,
                        SeasonTag.WINTER,
                        CrowdTag.BABY,
                        true,
                        false,
                        DifficultyLevel.EASY,
                        30))
                .thenReturn(42);

        CountRecipeQryExe qryExe = new CountRecipeQryExe(recipeRepository);

        int result = qryExe.execute(qry);

        assertEquals(42, result);
        verify(recipeRepository)
                .count(
                        "Soup",
                        RecipeType.SOUP,
                        SeasonTag.WINTER,
                        CrowdTag.BABY,
                        true,
                        false,
                        DifficultyLevel.EASY,
                        30);
    }
}
