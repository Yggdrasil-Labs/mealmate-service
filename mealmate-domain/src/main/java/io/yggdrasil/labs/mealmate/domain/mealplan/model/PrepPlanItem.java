package io.yggdrasil.labs.mealmate.domain.mealplan.model;

import java.math.BigDecimal;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PrepPriority;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PrepTaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 备菜计划条目。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrepPlanItem {

    private Long id;
    private Long prepPlanId;
    private String ingredientName;
    private BigDecimal quantity;
    private String unit;
    private String storageMethod;
    private PrepPriority priority;
    private PrepTaskStatus taskStatus;
    private String remark;
}
