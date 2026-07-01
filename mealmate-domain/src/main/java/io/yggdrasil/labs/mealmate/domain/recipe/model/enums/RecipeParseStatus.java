package io.yggdrasil.labs.mealmate.domain.recipe.model.enums;

import lombok.Getter;

/** AI 菜品解析会话状态。 */
@Getter
public enum RecipeParseStatus {
    PARSING("PARSING"),
    REFINING("REFINING"),
    READY_TO_CONFIRM("READY_TO_CONFIRM"),
    CONFIRMED("CONFIRMED");

    private final String code;

    RecipeParseStatus(String code) {
        this.code = code;
    }
}
