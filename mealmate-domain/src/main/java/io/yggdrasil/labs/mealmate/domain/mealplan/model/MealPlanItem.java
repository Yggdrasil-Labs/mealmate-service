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

    /** 是否被手动调整过。 */
    private boolean manuallyAdjusted;

    /** 累计调整次数。 */
    private int adjustCount;

    /** 调整当前条目的菜品，标记为手动调整并累加次数。 */
    public void adjust(Long newRecipeId) {
        this.recipeId = newRecipeId;
        this.manuallyAdjusted = true;
        this.adjustCount++;
    }
}
