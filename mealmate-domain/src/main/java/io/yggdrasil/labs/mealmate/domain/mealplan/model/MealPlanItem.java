package io.yggdrasil.labs.mealmate.domain.mealplan.model;

import java.time.LocalDate;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealPlanCrowdType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 餐计划条目。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanItem {

    private Long id;
    private Long planId;
    private LocalDate mealDate;
    private MealType mealType;
    private Long recipeId;
    private MealPlanCrowdType crowdType;
    private boolean weightLoss;
    private boolean babyMeal;
    private boolean duplicateFlag;
    private int sortOrder;
    private String remark;
}
