package io.yggdrasil.labs.mealmate.domain.recipe.model.enums;

import lombok.Getter;

/** 食材类型。 */
@Getter
public enum IngredientType {
    VEGETABLE("VEGETABLE"),
    MEAT("MEAT"),
    SEAFOOD("SEAFOOD"),
    GRAIN("GRAIN"),
    FRUIT("FRUIT"),
    DAIRY("DAIRY"),
    SEASONING("SEASONING"),
    OTHER("OTHER");

    private final String code;

    IngredientType(String code) {
        this.code = code;
    }
}
