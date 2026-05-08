package io.yggdrasil.labs.mealmate.app.recipe.executor;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.PageRecipeQry;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CountRecipeQryExe {

    private final RecipeRepository recipeRepository;

    public int execute(PageRecipeQry qry) {
        return recipeRepository.count(
                qry.getKeyword(),
                qry.getRecipeType(),
                qry.getSeasonTag(),
                qry.getCrowdTag(),
                qry.getBabyFriendly(),
                qry.getWeightLossFriendly(),
                qry.getDifficultyLevel(),
                qry.getMaxCookingTime());
    }
}
