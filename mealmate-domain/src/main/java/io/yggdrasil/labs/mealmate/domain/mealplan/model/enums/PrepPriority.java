package io.yggdrasil.labs.mealmate.domain.mealplan.model.enums;

import lombok.Getter;

/** 备菜优先级。 */
@Getter
public enum PrepPriority {
    HIGH("HIGH"),
    NORMAL("NORMAL"),
    LOW("LOW");

    private final String code;

    PrepPriority(String code) {
        this.code = code;
    }
}
