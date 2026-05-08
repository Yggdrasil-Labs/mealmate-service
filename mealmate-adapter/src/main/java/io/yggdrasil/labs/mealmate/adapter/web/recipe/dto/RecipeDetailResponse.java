package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.RecipeStatus;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.RecipeType;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.SeasonTag;
import lombok.Data;

@Data
@Schema(description = "Detailed recipe response model.")
public class RecipeDetailResponse {

    @Schema(description = "Recipe id.", example = "1001")
    private Long id;

    @Schema(description = "Recipe name.", example = "Pumpkin Soup")
    private String name;

    @Schema(description = "Recipe type.", example = "SOUP")
    private RecipeType recipeType;

    @Schema(description = "Recipe source type.", example = "MANUAL")
    private RecipeSourceType sourceType;

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
            description = "Cover image URL.",
            example = "https://cdn.example.com/recipes/pumpkin-soup.png")
    private String coverImageUrl;

    @Schema(description = "Whether the recipe is baby friendly.", example = "true")
    private Boolean babyFriendly;

    @Schema(description = "Whether the recipe is weight-loss friendly.", example = "false")
    private Boolean weightLossFriendly;

    @Schema(description = "Recipe status.", example = "ACTIVE")
    private RecipeStatus status;

    @Schema(description = "Ingredient list.")
    private List<RecipeIngredientResponse> ingredients;

    @Schema(description = "Step list.")
    private List<RecipeStepResponse> steps;

    @Schema(description = "Nutrition info.")
    private NutritionFactResponse nutritionFact;
}
