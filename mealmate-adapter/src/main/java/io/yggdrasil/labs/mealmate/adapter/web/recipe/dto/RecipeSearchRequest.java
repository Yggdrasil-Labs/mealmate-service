package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Query parameters for lightweight recipe search.")
public class RecipeSearchRequest {

    @Schema(description = "Keyword fuzzy matched against recipe name.", example = "Pumpkin")
    @NotBlank
    private String keyword;

    @Schema(description = "Maximum number of records returned.", example = "5")
    private Integer limit;
}
