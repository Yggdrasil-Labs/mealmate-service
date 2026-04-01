package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 辣度等级。婴儿角色下禁止 {@link #MILD}、{@link #MEDIUM}、{@link #HOT}，由领域服务校验。 */
@Getter
public enum SpicyLevel {
    NONE("NONE"),
    MILD("MILD"),
    MEDIUM("MEDIUM"),
    HOT("HOT");

    private final String code;

    SpicyLevel(String code) {
        this.code = code;
    }
}
