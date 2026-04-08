package io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** Web 协议层辣度偏好枚举。 */
@Schema(description = "Preferred spicy level in meals.")
public enum SpicyLevel {
    NONE, // 不辣
    MILD, // 微辣
    MEDIUM, // 中辣
    HOT // 重辣
}
