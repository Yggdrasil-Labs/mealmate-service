package io.yggdrasil.labs.mealmate.app.recipe.executor;

import java.util.List;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.recipe.assembler.RecipeAssembler;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.SearchRecipeQry;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SearchRecipeQryExe {

    private final RecipeRepository recipeRepository;
    private final RecipeAssembler recipeAssembler;

    public List<RecipeCO> execute(SearchRecipeQry qry) {
        return recipeAssembler.toRecipeCOList(
                recipeRepository.searchByKeyword(qry.getKeyword(), qry.getLimit()));
    }
}
