package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.IngredientType;
import lombok.Data;

@Data
@Schema(description = "Ingredient payload of a recipe.")
public class RecipeIngredientRequest {

    @Schema(description = "Ingredient name.", example = "Pumpkin")
    @NotBlank
    private String ingredientName;

    @Schema(description = "Ingredient type.", example = "VEGETABLE")
    private IngredientType ingredientType;

    @Schema(description = "Ingredient quantity.", example = "300")
    private BigDecimal quantity;

    @Schema(description = "Ingredient unit.", example = "g")
    private String unit;

    @Schema(description = "Whether the ingredient is the main ingredient.", example = "true")
    private Boolean mainIngredient;

    @Schema(description = "Sort order in the ingredient list.", example = "1")
    private Integer sortNo;
}
