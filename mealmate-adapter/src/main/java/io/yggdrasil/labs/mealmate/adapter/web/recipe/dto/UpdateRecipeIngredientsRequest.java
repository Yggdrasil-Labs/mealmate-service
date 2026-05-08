package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for replacing all ingredients of a recipe.")
public class UpdateRecipeIngredientsRequest {

    @Valid
    @NotEmpty
    @Schema(description = "New ingredient list.")
    private List<RecipeIngredientRequest> ingredients;
}
