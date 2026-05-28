package io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualAddItemCmd {

    private Long planId;
    @NotBlank private String recipeName;
    private LocalDate mealDate;
    private String mealType;
    private String crowdType;
}
