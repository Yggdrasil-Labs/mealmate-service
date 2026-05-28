package io.yggdrasil.labs.mealmate.app.mealplan.dto.co;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanItemCO {

    private Long itemId;
    private Long recipeId;
    private String recipeName;
    private String crowdType;
    private boolean isWeightLoss;
    private boolean isBabyMeal;
    private boolean duplicateFlag;
    private String coverImageUrl;
    private Integer cookingTimeMin;
    private int sortOrder;
}
