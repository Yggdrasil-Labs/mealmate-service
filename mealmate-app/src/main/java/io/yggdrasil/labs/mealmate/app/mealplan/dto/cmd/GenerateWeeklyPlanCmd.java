package io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateWeeklyPlanCmd {

    private Long familyId;
    @NotNull private LocalDate weekStartDate;
    private Boolean forceRegenerate;
}
