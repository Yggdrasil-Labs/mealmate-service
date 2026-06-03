package io.yggdrasil.labs.mealmate.app.mealplan.dto.co;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingItemCO {

    private Long id;
    private String ingredientName;
    private BigDecimal totalQuantity;
    private String unit;
    private boolean purchased;
    private int sortNo;
}
