package io.yggdrasil.labs.mealmate.domain.mealplan.model.enums;

import lombok.Getter;

/** 餐次类型。 */
@Getter
public enum MealType {
    BREAKFAST("BREAKFAST"),
    LUNCH("LUNCH"),
    DINNER("DINNER");

    private final String code;

    MealType(String code) {
        this.code = code;
    }
}
