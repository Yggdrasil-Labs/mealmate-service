package io.yggdrasil.labs.mealmate.app.mealplan.dto.qry;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetItemHistoryQry {

    @NotNull private Long planId;
    @NotNull private Long itemId;
}
