package io.yggdrasil.labs.mealmate.domain.mealplan.model.enums;

import lombok.Getter;

/** 备菜任务状态。 */
@Getter
public enum PrepTaskStatus {
    TODO("TODO"),
    DONE("DONE");

    private final String code;

    PrepTaskStatus(String code) {
        this.code = code;
    }
}
