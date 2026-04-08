package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 家庭启用状态；{@link #code} 与库表 {@code VARCHAR} 存储值一致。 */
@Getter
public enum FamilyStatus {
    ENABLED("ENABLED"), // 启用
    DISABLED("DISABLED"); // 停用

    private final String code;

    FamilyStatus(String code) {
        this.code = code;
    }
}
