package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 成员在用状态；{@link #code} 为持久化存储码。 */
@Getter
public enum MemberStatus {
    ACTIVE("ACTIVE"), // 在用
    INACTIVE("INACTIVE"); // 停用

    private final String code;

    MemberStatus(String code) {
        this.code = code;
    }
}
