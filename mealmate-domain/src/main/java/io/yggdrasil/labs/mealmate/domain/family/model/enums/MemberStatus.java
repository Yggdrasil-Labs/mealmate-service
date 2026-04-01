package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 成员在用状态；{@link #code} 为持久化存储码。 */
@Getter
public enum MemberStatus {
    ACTIVE("ACTIVE"),
    INACTIVE("INACTIVE");

    private final String code;

    MemberStatus(String code) {
        this.code = code;
    }
}
