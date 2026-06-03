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
public class PrepPlanCO {

    private Long id;
    private Long planId;
    private String pushStatus;
    private List<PrepPlanItemCO> items;
}
