package io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** AI 周餐计划生成命令。包含家庭 ID、周起始日期和可选的用户提示。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMealPlanGenerateCmd {

    /** 目标家庭 ID。 */
    @NotNull private Long familyId;

    /** 周计划起始日期（周一）。 */
    @NotNull private LocalDate weekStartDate;

    /** 用户自由输入的额外提示，如"这周想吃清淡一些"。 */
    private String userHint;
}
