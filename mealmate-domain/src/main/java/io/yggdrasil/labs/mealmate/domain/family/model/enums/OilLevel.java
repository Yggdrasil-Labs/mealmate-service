package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 用油量等级。婴儿角色下禁止 {@link #RICH}。 */
@Getter
public enum OilLevel {
    LIGHT("LIGHT"), // 少油
    MODERATE("MODERATE"), // 适中
    RICH("RICH"); // 重油

    private final String code;

    OilLevel(String code) {
        this.code = code;
    }
}
