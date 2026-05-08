package io.yggdrasil.labs.mealmate.domain.recipe.model.enums;

import lombok.Getter;

/** 菜谱适用人群标签。 */
@Getter
public enum CrowdTag {
    GENERAL("GENERAL"),
    BABY("BABY"),
    WEIGHT_LOSS("WEIGHT_LOSS");

    private final String code;

    CrowdTag(String code) {
        this.code = code;
    }
}
