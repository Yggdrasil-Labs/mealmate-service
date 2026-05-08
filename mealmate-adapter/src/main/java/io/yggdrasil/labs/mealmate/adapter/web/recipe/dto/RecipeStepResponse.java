package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Step view of a recipe.")
public class RecipeStepResponse {

    @Schema(description = "Step id.", example = "201")
    private Long id;

    @Schema(description = "Recipe id.", example = "1001")
    private Long recipeId;

    @Schema(description = "Step number.", example = "1")
    private Integer stepNo;

    @Schema(description = "Step content.", example = "Steam pumpkin until soft.")
    private String content;

    @Schema(
            description = "Step image URL.",
            example = "https://cdn.example.com/steps/pumpkin-1.png")
    private String imageUrl;
}
