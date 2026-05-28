package io.yggdrasil.labs.mealmate.domain.mealplan.model.enums;

import lombok.Getter;

/** 计划来源。 */
@Getter
public enum PlanSource {
    MANUAL("MANUAL"),
    AI_GENERATED("AI_GENERATED");

    private final String code;

    PlanSource(String code) {
        this.code = code;
    }
}
