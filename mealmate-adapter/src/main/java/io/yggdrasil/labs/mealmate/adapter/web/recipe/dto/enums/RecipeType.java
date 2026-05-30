package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of recipe.")
public enum RecipeType {
    HOME_COOKING,
    MAIN_DISH,
    SIDE_DISH,
    SOUP,
    STAPLE,
    SNACK,
    DESSERT,
    OTHER
}
