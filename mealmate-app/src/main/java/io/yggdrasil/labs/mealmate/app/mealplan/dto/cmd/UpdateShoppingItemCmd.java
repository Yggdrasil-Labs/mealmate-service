package io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShoppingItemCmd {

    private Long planId;
    private Long itemId;
    @NotNull private Boolean purchased;
}
