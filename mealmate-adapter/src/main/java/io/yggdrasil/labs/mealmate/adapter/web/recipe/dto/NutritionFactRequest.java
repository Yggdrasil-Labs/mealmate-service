package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto;

import java.math.BigDecimal;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Nutrition payload of a recipe.")
public class NutritionFactRequest {

    @Schema(description = "Calories.", example = "220")
    private BigDecimal calories;

    @Schema(description = "Protein.", example = "8.5")
    private BigDecimal protein;

    @Schema(description = "Fat.", example = "4.2")
    private BigDecimal fat;

    @Schema(description = "Carbohydrate.", example = "35.6")
    private BigDecimal carbohydrate;

    @Schema(description = "Fiber.", example = "5.1")
    private BigDecimal fiber;

    @Schema(description = "Calcium.", example = "56")
    private BigDecimal calcium;

    @Schema(description = "Sodium.", example = "120")
    private BigDecimal sodium;

    @Schema(
            description = "Extended structured nutrition fields.",
            example = "{\"vitaminC\":\"high\"}")
    private Map<String, Object> nutritionJson;
}
