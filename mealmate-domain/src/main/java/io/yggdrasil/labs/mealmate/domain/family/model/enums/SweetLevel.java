package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 甜度偏好等级。 */
@Getter
public enum SweetLevel {
    NONE("NONE"),
    LIGHT("LIGHT"),
    MODERATE("MODERATE"),
    SWEET("SWEET");

    private final String code;

    SweetLevel(String code) {
        this.code = code;
    }
}
