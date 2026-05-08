package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Preferred season tag for the recipe.")
public enum SeasonTag {
    SPRING,
    SUMMER,
    AUTUMN,
    WINTER,
    ALL_SEASON
}
