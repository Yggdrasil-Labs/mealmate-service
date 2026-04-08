package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 甜度偏好等级。 */
@Getter
public enum SweetLevel {
    NONE("NONE"), // 无糖/不甜
    LIGHT("LIGHT"), // 微甜
    MODERATE("MODERATE"), // 适中
    SWEET("SWEET"); // 偏甜

    private final String code;

    SweetLevel(String code) {
        this.code = code;
    }
}
