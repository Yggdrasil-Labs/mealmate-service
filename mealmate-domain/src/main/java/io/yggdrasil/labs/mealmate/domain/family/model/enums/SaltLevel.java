package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 咸度等级。婴儿角色下禁止 {@link #SALTY}。 */
@Getter
public enum SaltLevel {
    LIGHT("LIGHT"), // 少盐
    MODERATE("MODERATE"), // 适中
    SALTY("SALTY"); // 偏咸

    private final String code;

    SaltLevel(String code) {
        this.code = code;
    }
}
