package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 成员角色。同一家庭可有多个 {@link #BABY}，业务上不对婴儿数量做唯一约束。 */
@Getter
public enum MemberRoleType {
    ADULT("ADULT"), // 成人
    BABY("BABY"), // 婴幼儿
    GUEST("GUEST"); // 访客/临时成员

    private final String code;

    MemberRoleType(String code) {
        this.code = code;
    }
}
