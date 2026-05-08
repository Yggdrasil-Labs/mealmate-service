package io.yggdrasil.labs.mealmate.app.recipe.dto.qry;

import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRecipeQry {

    private String keyword;
    private RecipeType recipeType;
    private SeasonTag seasonTag;
    private CrowdTag crowdTag;
    private Boolean babyFriendly;
    private Boolean weightLossFriendly;
    private DifficultyLevel difficultyLevel;
    private Integer maxCookingTime;
    private Integer pageNum;
    private Integer pageSize;
}
