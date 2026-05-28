package io.yggdrasil.labs.mealmate.app.mealplan.dto.co;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmPlanCO {

    private Long planId;
    private String status;
    private Long prepPlanId;
    private int prepItemCount;
    private int shoppingItemCount;
}
