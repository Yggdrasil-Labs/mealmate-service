package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cooking difficulty level.")
public enum DifficultyLevel {
    EASY,
    MEDIUM,
    HARD
}
