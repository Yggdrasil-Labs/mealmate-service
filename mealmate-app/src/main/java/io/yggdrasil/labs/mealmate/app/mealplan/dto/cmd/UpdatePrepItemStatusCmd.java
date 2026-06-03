package io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePrepItemStatusCmd {

    private Long planId;
    private Long itemId;
    @NotBlank private String status;
}
