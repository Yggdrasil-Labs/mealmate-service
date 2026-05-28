package io.yggdrasil.labs.mealmate.app.mealplan.dto.qry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPrepPlanQry {

    private Long planId;
}
