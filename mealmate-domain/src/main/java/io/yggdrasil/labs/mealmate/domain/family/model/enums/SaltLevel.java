package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 咸度等级。婴儿角色下禁止 {@link #SALTY}。 */
@Getter
public enum SaltLevel {
    LIGHT("LIGHT"),
    MODERATE("MODERATE"),
    SALTY("SALTY");

    private final String code;

    SaltLevel(String code) {
        this.code = code;
    }
}
