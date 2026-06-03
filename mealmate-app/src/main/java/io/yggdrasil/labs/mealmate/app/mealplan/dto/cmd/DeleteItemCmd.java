package io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteItemCmd {

    private Long planId;
    private Long itemId;
}
