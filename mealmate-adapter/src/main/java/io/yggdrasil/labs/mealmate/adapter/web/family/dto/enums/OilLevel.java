package io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** Web 协议层用油偏好枚举。 */
@Schema(description = "Preferred oil level in meals.")
public enum OilLevel {
    LIGHT, // 少油
    MODERATE, // 适中
    RICH // 重油
}
