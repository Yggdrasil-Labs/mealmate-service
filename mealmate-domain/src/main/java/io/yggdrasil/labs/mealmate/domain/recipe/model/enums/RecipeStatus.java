package io.yggdrasil.labs.mealmate.domain.recipe.model.enums;

import lombok.Getter;

/** 菜谱状态。 */
@Getter
public enum RecipeStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String code;

    RecipeStatus(String code) {
        this.code = code;
    }
}
