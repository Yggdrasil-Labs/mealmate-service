package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.RecipeType;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.SeasonTag;
import lombok.Data;

@Data
@Schema(description = "Request payload for creating a recipe.")
public class CreateRecipeRequest {

    @Schema(description = "Recipe name.", example = "Pumpkin Soup")
    @NotBlank
    private String name;

    @Schema(description = "Recipe type.", example = "SOUP")
    @NotNull
    private RecipeType recipeType;

    @Schema(description = "Season tag.", example = "AUTUMN")
    private SeasonTag seasonTag;

    @Schema(description = "Crowd tag.", example = "BABY")
    private CrowdTag crowdTag;

    @Schema(description = "Taste tags.", example = "[\"sweet\",\"soft\"]")
    private List<String> tasteTags;

    @Schema(description = "Difficulty level.", example = "EASY")
    private DifficultyLevel difficultyLevel;

    @Schema(description = "Cooking time in minutes.", example = "25")
    private Integer cookingTimeMin;

    @Schema(
            description = "Cover image URL of the recipe.",
            example = "https://cdn.example.com/recipes/pumpkin-soup.png")
    private String coverImageUrl;

    @Schema(description = "Whether the recipe is baby friendly.", example = "true")
    private Boolean babyFriendly;

    @Schema(description = "Whether the recipe is weight-loss friendly.", example = "false")
    private Boolean weightLossFriendly;

    @Valid
    @Schema(description = "Ingredient list of the recipe.")
    private List<RecipeIngredientRequest> ingredients;

    @Valid
    @Schema(description = "Step list of the recipe.")
    private List<RecipeStepRequest> steps;

    @Valid
    @Schema(description = "Nutrition info of the recipe.")
    private NutritionFactRequest nutritionFact;
}
