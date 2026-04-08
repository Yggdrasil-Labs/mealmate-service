package io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** Web 协议层家庭状态枚举。用于请求/响应契约，避免 adapter 直接依赖 domain 枚举。 */
@Schema(description = "Lifecycle status of a family.")
public enum FamilyStatus {
    ENABLED, // 启用
    DISABLED // 停用
}
