package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 成员角色。同一家庭可有多个 {@link #BABY}，业务上不对婴儿数量做唯一约束。 */
@Getter
public enum MemberRoleType {
    ADULT("ADULT"),
    BABY("BABY"),
    GUEST("GUEST");

    private final String code;

    MemberRoleType(String code) {
        this.code = code;
    }
}
