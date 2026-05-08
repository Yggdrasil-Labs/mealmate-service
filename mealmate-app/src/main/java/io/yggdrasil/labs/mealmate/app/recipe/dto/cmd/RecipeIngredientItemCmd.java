package io.yggdrasil.labs.mealmate.app.recipe.dto.cmd;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.IngredientType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredientItemCmd {

    @NotBlank private String ingredientName;

    private IngredientType ingredientType;
    private BigDecimal quantity;
    private String unit;
    private Boolean mainIngredient;
    private Integer sortNo;
}
