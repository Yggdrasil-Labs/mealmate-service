package io.yggdrasil.labs.mealmate.domain.mealplan.model.enums;

import lombok.Getter;

/** 周计划状态。 */
@Getter
public enum PlanStatus {
    DRAFT("DRAFT"),
    CONFIRMED("CONFIRMED"),
    ARCHIVED("ARCHIVED");

    private final String code;

    PlanStatus(String code) {
        this.code = code;
    }
}
