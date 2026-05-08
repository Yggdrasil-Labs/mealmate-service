package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.RecipeType;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.SeasonTag;
import lombok.Data;

@Data
@Schema(description = "Query parameters for paging recipes.")
public class RecipePageRequest {

    @Schema(description = "Keyword fuzzy matched against recipe name.", example = "Soup")
    private String keyword;

    @Schema(description = "Recipe type filter.", example = "SOUP")
    private RecipeType recipeType;

    @Schema(description = "Season tag filter.", example = "WINTER")
    private SeasonTag seasonTag;

    @Schema(description = "Crowd tag filter.", example = "BABY")
    private CrowdTag crowdTag;

    @Schema(name = "isBabyFriendly", description = "Whether recipe must be baby friendly.")
    private Boolean isBabyFriendly;

    @Schema(
            name = "isWeightLossFriendly",
            description = "Whether recipe must be weight-loss friendly.")
    private Boolean isWeightLossFriendly;

    @Schema(description = "Difficulty level filter.", example = "EASY")
    private DifficultyLevel difficultyLevel;

    @Schema(description = "Maximum cooking time in minutes.", example = "30")
    private Integer maxCookingTime;

    @Schema(description = "Page number starting from 1.", example = "1")
    private Integer pageNum;

    @Schema(description = "Page size.", example = "10")
    private Integer pageSize;
}
