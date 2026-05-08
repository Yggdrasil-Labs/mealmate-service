package io.yggdrasil.labs.mealmate.domain.recipe.model.enums;

import lombok.Getter;

/** 烹饪难度。 */
@Getter
public enum DifficultyLevel {
    EASY("EASY"),
    MEDIUM("MEDIUM"),
    HARD("HARD");

    private final String code;

    DifficultyLevel(String code) {
        this.code = code;
    }
}
