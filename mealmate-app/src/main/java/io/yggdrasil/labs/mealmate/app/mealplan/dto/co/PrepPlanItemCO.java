package io.yggdrasil.labs.mealmate.app.mealplan.dto.co;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrepPlanItemCO {

    private Long id;
    private String ingredientName;
    private BigDecimal quantity;
    private String unit;
    private String storageMethod;
    private String priority;
    private String taskStatus;
}
