package io.yggdrasil.labs.mealmate.app.recipe.dto.co;

import java.math.BigDecimal;

import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.IngredientType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientCO {

    private Long id;
    private Long recipeId;
    private String ingredientName;
    private IngredientType ingredientType;
    private BigDecimal quantity;
    private String unit;
    private Boolean mainIngredient;
    private Integer sortNo;
}
