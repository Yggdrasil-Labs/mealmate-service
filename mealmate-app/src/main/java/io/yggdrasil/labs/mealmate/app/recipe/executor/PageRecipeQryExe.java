package io.yggdrasil.labs.mealmate.app.recipe.executor;

import java.util.List;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.recipe.assembler.RecipeAssembler;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.PageRecipeQry;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeQueryCriteria;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PageRecipeQryExe {

    private final RecipeRepository recipeRepository;
    private final RecipeAssembler recipeAssembler;

    public List<RecipeCO> execute(PageRecipeQry qry) {
        return recipeAssembler.toRecipeCOList(recipeRepository.page(toCriteria(qry)));
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
                .pageNum(qry.getPageNum())
                .pageSize(qry.getPageSize())
                .build();
    }
}
