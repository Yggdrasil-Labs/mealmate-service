package io.yggdrasil.labs.mealmate.domain.mealplan.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanSource;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 周餐计划聚合根。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyMealPlan {

    private Long id;
    private Long familyId;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private PlanStatus status;
    private PlanSource planSource;
    private String ruleSnapshotJson;
    private String remark;
    private LocalDateTime generatedTime;
    private List<MealPlanItem> items;
}
