package io.yggdrasil.labs.mealmate.domain.mealplan.model.enums;

import lombok.Getter;

/** 用餐人群类型。 */
@Getter
public enum MealPlanCrowdType {
    FAMILY("FAMILY"),
    WIFE("WIFE"),
    HUSBAND("HUSBAND"),
    BABY("BABY"),
    WIFE_WEIGHT_LOSS("WIFE_WEIGHT_LOSS");

    private final String code;

    MealPlanCrowdType(String code) {
        this.code = code;
    }
}
