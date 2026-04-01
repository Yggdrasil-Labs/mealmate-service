package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 成员饮食目标类型（如减脂、增肌），存储为稳定英文码。 */
@Getter
public enum MemberTargetType {
    BALANCED("BALANCED"),
    WEIGHT_LOSS("WEIGHT_LOSS"),
    MUSCLE_GAIN("MUSCLE_GAIN"),
    HEALTH_MANAGEMENT("HEALTH_MANAGEMENT");

    private final String code;

    MemberTargetType(String code) {
        this.code = code;
    }
}
