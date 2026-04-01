package io.yggdrasil.labs.mealmate.domain.family.model.enums;

import lombok.Getter;

/** 生理/社会性别展示用枚举；{@link #code} 与库表一致。 */
@Getter
public enum GenderType {
    MALE("MALE"),
    FEMALE("FEMALE"),
    UNKNOWN("UNKNOWN");

    private final String code;

    GenderType(String code) {
        this.code = code;
    }
}
