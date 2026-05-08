package io.yggdrasil.labs.mealmate.app.recipe.dto.co;

import java.math.BigDecimal;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionFactCO {

    private Long id;
    private Long recipeId;
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal fat;
    private BigDecimal carbohydrate;
    private BigDecimal fiber;
    private BigDecimal calcium;
    private BigDecimal sodium;
    private Map<String, Object> nutritionJson;
}
