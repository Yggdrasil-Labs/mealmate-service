package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of the recipe.")
public enum RecipeStatus {
    ACTIVE,
    INACTIVE
}
