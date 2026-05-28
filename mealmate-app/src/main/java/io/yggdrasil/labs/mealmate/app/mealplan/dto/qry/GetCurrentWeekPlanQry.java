package io.yggdrasil.labs.mealmate.app.mealplan.dto.qry;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetCurrentWeekPlanQry {

    private Long familyId;

    /** 指定周起始日期，为空则取当前周。 */
    private LocalDate weekStartDate;
}
