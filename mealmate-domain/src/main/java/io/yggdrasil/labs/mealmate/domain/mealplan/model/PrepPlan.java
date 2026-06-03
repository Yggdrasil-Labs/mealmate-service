package io.yggdrasil.labs.mealmate.domain.mealplan.model;

import java.time.LocalDateTime;
import java.util.List;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PushStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 备菜计划。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrepPlan {

    private Long id;
    private Long planId;
    private PushStatus pushStatus;
    private LocalDateTime generatedTime;
    private String remark;
    private List<PrepPlanItem> items;
}
