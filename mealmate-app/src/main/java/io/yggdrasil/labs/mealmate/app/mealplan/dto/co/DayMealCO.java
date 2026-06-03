package io.yggdrasil.labs.mealmate.app.mealplan.dto.co;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayMealCO {

    private String date;
    private List<MealPlanItemCO> breakfast;
    private List<MealPlanItemCO> lunch;
    private List<MealPlanItemCO> dinner;
}
