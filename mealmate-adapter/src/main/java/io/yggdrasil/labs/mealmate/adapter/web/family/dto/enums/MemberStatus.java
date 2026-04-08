package io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** Web 协议层成员状态枚举。 */
@Schema(description = "Lifecycle status of a family member.")
public enum MemberStatus {
    ACTIVE, // 在用
    INACTIVE // 停用
}
