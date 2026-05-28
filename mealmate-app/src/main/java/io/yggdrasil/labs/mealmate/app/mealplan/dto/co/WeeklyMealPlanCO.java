package io.yggdrasil.labs.mealmate.app.mealplan.dto.co;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyMealPlanCO {

    private Long planId;
    private String weekStartDate;
    private String weekEndDate;
    private String status;
    private String planSource;
    private Map<String, DayMealCO> dayMeals;
}
