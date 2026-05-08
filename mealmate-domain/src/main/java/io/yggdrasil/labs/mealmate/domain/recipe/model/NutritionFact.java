package io.yggdrasil.labs.mealmate.domain.recipe.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 菜谱营养值对象。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionFact {

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
