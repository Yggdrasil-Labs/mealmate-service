package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of ingredient.")
public enum IngredientType {
    VEGETABLE,
    MEAT,
    SEAFOOD,
    GRAIN,
    FRUIT,
    DAIRY,
    SEASONING,
    OTHER
}
