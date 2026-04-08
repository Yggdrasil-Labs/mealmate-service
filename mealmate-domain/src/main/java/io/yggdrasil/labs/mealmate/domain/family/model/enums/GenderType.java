package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 生理/社会性别展示用枚举；{@link #code} 与库表一致。 */
@Getter
public enum GenderType {
    MALE("MALE"), // 男
    FEMALE("FEMALE"), // 女
    UNKNOWN("UNKNOWN"); // 未知/未设置

    private final String code;

    GenderType(String code) {
        this.code = code;
    }
}
