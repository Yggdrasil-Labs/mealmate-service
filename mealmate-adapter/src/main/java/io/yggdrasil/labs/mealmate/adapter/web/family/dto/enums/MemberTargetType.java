package io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** Web 协议层成员目标类型枚举（如减脂、增肌）。 */
@Schema(description = "Dietary or health target of a family member.")
public enum MemberTargetType {
    BALANCED, // 均衡饮食
    WEIGHT_LOSS, // 减脂/减重
    MUSCLE_GAIN, // 增肌
    HEALTH_MANAGEMENT // 健康管理
}
