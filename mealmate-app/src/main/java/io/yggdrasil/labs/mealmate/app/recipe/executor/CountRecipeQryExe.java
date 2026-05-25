package io.yggdrasil.labs.mealmate.app.recipe.executor;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.PageRecipeQry;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeQueryCriteria;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CountRecipeQryExe {

    private final RecipeRepository recipeRepository;

    public int execute(PageRecipeQry qry) {
        return recipeRepository.count(toCriteria(qry));
    }

    private RecipeQueryCriteria toCriteria(PageRecipeQry qry) {
        return RecipeQueryCriteria.builder()
                .keyword(qry.getKeyword())
                .recipeType(qry.getRecipeType())
                .seasonTag(qry.getSeasonTag())
                .crowdTag(qry.getCrowdTag())
                .babyFriendly(qry.getBabyFriendly())
                .weightLossFriendly(qry.getWeightLossFriendly())
                .difficultyLevel(qry.getDifficultyLevel())
                .maxCookingTime(qry.getMaxCookingTime())
                .build();
    }
}
