package io.yggdrasil.labs.mealmate.domain.recipe.model.enums;

import lombok.Getter;

/** 菜谱类型。 */
@Getter
public enum RecipeType {
    MAIN_DISH("MAIN_DISH"),
    SIDE_DISH("SIDE_DISH"),
    SOUP("SOUP"),
    SNACK("SNACK"),
    DESSERT("DESSERT"),
    OTHER("OTHER");

    private final String code;

    RecipeType(String code) {
        this.code = code;
    }
}
