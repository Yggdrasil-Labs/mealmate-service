package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums.IngredientType;
import lombok.Data;

@Data
@Schema(description = "Ingredient view of a recipe.")
public class RecipeIngredientResponse {

    @Schema(description = "Ingredient id.", example = "101")
    private Long id;

    @Schema(description = "Recipe id.", example = "1001")
    private Long recipeId;

    @Schema(description = "Ingredient name.", example = "Pumpkin")
    private String ingredientName;

    @Schema(description = "Ingredient type.", example = "VEGETABLE")
    private IngredientType ingredientType;

    @Schema(description = "Quantity.", example = "300")
    private BigDecimal quantity;

    @Schema(description = "Unit.", example = "g")
    private String unit;

    @Schema(description = "Whether this is the main ingredient.", example = "true")
    private Boolean mainIngredient;

    @Schema(description = "Sort order.", example = "1")
    private Integer sortNo;
}
