package io.yggdrasil.labs.mealmate.domain.mealplan.model.enums;

import lombok.Getter;

/** 推送状态。 */
@Getter
public enum PushStatus {
    INIT("INIT"),
    SENT("SENT"),
    FAILED("FAILED");

    private final String code;

    PushStatus(String code) {
        this.code = code;
    }
}
