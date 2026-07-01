package io.yggdrasil.labs.mealmate.app.recipe.dto.cmd;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecipeCmd {

    @NotBlank private String name;

    @NotNull private RecipeType recipeType;
    private SeasonTag seasonTag;
    private CrowdTag crowdTag;
    private List<String> tasteTags;
    private DifficultyLevel difficultyLevel;
    private Integer cookingTimeMin;
    private String coverImageUrl;
    private Boolean babyFriendly;
    private Boolean weightLossFriendly;

    @Valid private List<RecipeIngredientItemCmd> ingredients;

    @Valid private List<RecipeStepItemCmd> steps;

    @Valid private NutritionFactCmd nutritionFact;

    /** 菜品来源类型。AI 录入时由 Executor 自动设置，可空 */
    private RecipeSourceType sourceType;
}
