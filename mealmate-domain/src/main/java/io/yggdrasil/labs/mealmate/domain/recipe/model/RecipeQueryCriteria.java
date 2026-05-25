package io.yggdrasil.labs.mealmate.domain.recipe.model;

import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 菜谱查询条件值对象。封装分页筛选参数，避免 Repository 方法参数膨胀。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeQueryCriteria {

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
