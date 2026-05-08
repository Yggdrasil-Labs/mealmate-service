package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.RecipeStatus;
import lombok.Data;

@Data
@Schema(description = "Request payload for updating recipe status.")
public class UpdateRecipeStatusRequest {

    @Schema(description = "New recipe status.", example = "INACTIVE")
    @NotNull
    private RecipeStatus status;
}
