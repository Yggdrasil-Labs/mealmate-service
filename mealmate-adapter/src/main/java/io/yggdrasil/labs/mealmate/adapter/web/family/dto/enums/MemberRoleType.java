package io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/** Web 协议层成员角色枚举。 */
@Schema(description = "Role of a family member in the household.")
public enum MemberRoleType {
    ADULT, // 成人
    BABY, // 婴幼儿
    GUEST // 访客/临时成员
}
