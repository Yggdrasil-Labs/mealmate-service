package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 成员饮食目标类型（如减脂、增肌），存储为稳定英文码。 */
@Getter
public enum MemberTargetType {
    BALANCED("BALANCED"), // 均衡饮食
    WEIGHT_LOSS("WEIGHT_LOSS"), // 减脂/减重
    MUSCLE_GAIN("MUSCLE_GAIN"), // 增肌
    HEALTH_MANAGEMENT("HEALTH_MANAGEMENT"); // 健康管理

    private final String code;

    MemberTargetType(String code) {
        this.code = code;
    }
}
