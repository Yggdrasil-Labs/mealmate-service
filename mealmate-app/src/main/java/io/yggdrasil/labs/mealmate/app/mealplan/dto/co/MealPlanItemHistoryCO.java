package io.yggdrasil.labs.mealmate.app.mealplan.dto.co;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MealPlanItemHistoryCO {

    private Long historyId;
    private String oldRecipeName;
    private String newRecipeName;
    private String adjustReason;
    private String adjustedAt;
}
