package io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** Web 协议层甜度偏好枚举。 */
@Schema(description = "Preferred sweetness level in meals.")
public enum SweetLevel {
    NONE, // 无糖/不甜
    LIGHT, // 微甜
    MODERATE, // 适中
    SWEET // 偏甜
}
