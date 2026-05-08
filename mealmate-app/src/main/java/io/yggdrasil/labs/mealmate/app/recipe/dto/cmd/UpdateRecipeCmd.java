package io.yggdrasil.labs.mealmate.app.recipe.dto.cmd;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
public class UpdateRecipeCmd {

    @NotNull private Long recipeId;

    @NotBlank private String name;

    private RecipeType recipeType;
    private SeasonTag seasonTag;
    private CrowdTag crowdTag;
    private List<String> tasteTags;
    private DifficultyLevel difficultyLevel;
    private Integer cookingTimeMin;
    private String coverImageUrl;
    private Boolean babyFriendly;
    private Boolean weightLossFriendly;
}
