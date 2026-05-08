package io.yggdrasil.labs.mealmate.domain.recipe.model.enums;

import lombok.Getter;

/** 菜谱来源类型。 */
@Getter
public enum RecipeSourceType {
    MANUAL("MANUAL"),
    AI_GENERATED("AI_GENERATED"),
    SYSTEM("SYSTEM");

    private final String code;

    RecipeSourceType(String code) {
        this.code = code;
    }
}
