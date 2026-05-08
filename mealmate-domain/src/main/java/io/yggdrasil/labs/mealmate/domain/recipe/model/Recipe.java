package io.yggdrasil.labs.mealmate.domain.recipe.model;

import java.time.LocalDateTime;
import java.util.List;

import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 菜谱聚合根。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    private Long id;
    private String name;
    private RecipeType recipeType;
    private RecipeSourceType sourceType;
    private SeasonTag seasonTag;
    private CrowdTag crowdTag;
    private List<String> tasteTags;
    private DifficultyLevel difficultyLevel;
    private Integer cookingTimeMin;
    private String coverImageUrl;
    private Boolean babyFriendly;
    private Boolean weightLossFriendly;
    private RecipeStatus status;
    private List<RecipeIngredient> ingredients;
    private List<RecipeStep> steps;
    private NutritionFact nutritionFact;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Long deleted;
}
