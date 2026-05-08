package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Source type of the recipe.")
public enum RecipeSourceType {
    MANUAL,
    AI_GENERATED,
    SYSTEM
}
