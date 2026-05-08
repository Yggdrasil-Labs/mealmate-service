package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for updating recipe nutrition.")
public class UpdateRecipeNutritionRequest {

    @Valid
    @Schema(description = "Nutrition information.")
    private NutritionFactRequest nutritionFact;
}
