package io.yggdrasil.labs.mealmate.domain.recipe.model.enums;

import lombok.Getter;

/** 菜谱季节标签。 */
@Getter
public enum SeasonTag {
    SPRING("SPRING"),
    SUMMER("SUMMER"),
    AUTUMN("AUTUMN"),
    WINTER("WINTER"),
    ALL_SEASON("ALL_SEASON");

    private final String code;

    SeasonTag(String code) {
        this.code = code;
    }
}
