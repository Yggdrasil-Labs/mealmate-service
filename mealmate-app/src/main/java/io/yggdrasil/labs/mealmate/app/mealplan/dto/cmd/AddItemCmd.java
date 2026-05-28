package io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddItemCmd {

    private Long planId;
    @NotNull private Long recipeId;
    @NotNull private LocalDate mealDate;
    @NotBlank private String mealType;
    private String crowdType;
}
