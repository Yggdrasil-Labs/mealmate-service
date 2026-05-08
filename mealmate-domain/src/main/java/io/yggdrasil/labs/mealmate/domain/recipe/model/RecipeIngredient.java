package io.yggdrasil.labs.mealmate.domain.recipe.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.IngredientType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 菜谱食材子对象。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeIngredient {

    private Long id;
    private Long recipeId;
    private String ingredientName;
    private IngredientType ingredientType;
    private BigDecimal quantity;
    private String unit;
    private Boolean mainIngredient;
    private Integer sortNo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
