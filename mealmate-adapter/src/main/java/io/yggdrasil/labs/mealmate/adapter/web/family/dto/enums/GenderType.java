package io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** Web 协议层成员性别枚举。 */
@Schema(description = "Gender of a family member.")
public enum GenderType {
    MALE, // 男
    FEMALE, // 女
    UNKNOWN // 未知/未设置
}
