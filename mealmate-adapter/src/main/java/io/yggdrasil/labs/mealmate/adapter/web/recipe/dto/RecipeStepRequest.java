package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Step payload of a recipe.")
public class RecipeStepRequest {

    @Schema(description = "Step number.", example = "1")
    private Integer stepNo;

    @Schema(description = "Step content.", example = "Steam pumpkin until soft.")
    @NotBlank
    private String content;

    @Schema(
            description = "Optional image URL for this step.",
            example = "https://cdn.example.com/steps/pumpkin-1.png")
    private String imageUrl;
}
