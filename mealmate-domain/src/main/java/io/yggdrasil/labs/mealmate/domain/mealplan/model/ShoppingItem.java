package io.yggdrasil.labs.mealmate.domain.mealplan.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 采购清单条目。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingItem {

    private Long id;
    private Long planId;
    private String ingredientName;
    private BigDecimal totalQuantity;
    private String unit;
    private boolean purchased;
    private int sortNo;
    private String remark;
}
