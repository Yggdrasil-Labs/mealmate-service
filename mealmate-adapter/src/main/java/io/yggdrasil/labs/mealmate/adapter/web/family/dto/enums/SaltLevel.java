package io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** Web 协议层咸度偏好枚举。 */
@Schema(description = "Preferred saltiness level in meals.")
public enum SaltLevel {
    LIGHT, // 少盐
    MODERATE, // 适中
    SALTY // 偏咸
}
