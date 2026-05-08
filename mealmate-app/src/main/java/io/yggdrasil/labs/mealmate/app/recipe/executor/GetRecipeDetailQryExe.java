package io.yggdrasil.labs.mealmate.app.recipe.executor;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.recipe.assembler.RecipeAssembler;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeDetailCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.GetRecipeDetailQry;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetRecipeDetailQryExe {

    private final RecipeRepository recipeRepository;
    private final RecipeAssembler recipeAssembler;

    public RecipeDetailCO execute(GetRecipeDetailQry qry) {
        return recipeRepository
                .findById(qry.getRecipeId())
                .map(recipeAssembler::toRecipeDetailCO)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
    }
}
