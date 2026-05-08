package io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Suitable crowd tag for the recipe.")
public enum CrowdTag {
    GENERAL,
    BABY,
    WEIGHT_LOSS
}
