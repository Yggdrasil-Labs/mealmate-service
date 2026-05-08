package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request payload for replacing all steps of a recipe.")
public class UpdateRecipeStepsRequest {

    @Valid
    @Schema(description = "New step list.")
    private List<RecipeStepRequest> steps;
}
