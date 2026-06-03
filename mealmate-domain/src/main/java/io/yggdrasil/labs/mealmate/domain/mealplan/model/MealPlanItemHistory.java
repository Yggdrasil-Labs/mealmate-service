package io.yggdrasil.labs.mealmate.domain.mealplan.model;

import java.time.LocalDateTime;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.AdjustReason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 餐计划条目调整历史记录。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanItemHistory {

    private Long id;
    private Long itemId;
    private Long planId;
    private Long oldRecipeId;
    private Long newRecipeId;
    private AdjustReason adjustReason;
    private LocalDateTime adjustedAt;
    private Long adjustedBy;
}
