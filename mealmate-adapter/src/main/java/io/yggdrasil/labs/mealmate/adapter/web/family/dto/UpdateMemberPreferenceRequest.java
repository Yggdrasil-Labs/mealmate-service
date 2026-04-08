package io.yggdrasil.labs.mealmate.adapter.web.family.dto;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums.OilLevel;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums.SaltLevel;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums.SpicyLevel;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums.SweetLevel;
import lombok.Data;

@Data
@Schema(description = "Request payload for updating a member's taste preference and restrictions.")
public class UpdateMemberPreferenceRequest {

    @Schema(
            description = "Taste preference tags of the member.",
            example = "[\"light\",\"home-style\"]")
    private List<String> tasteTags;

    @Schema(
            description = "Ingredients the member prefers to avoid.",
            example = "[\"cilantro\",\"celery\"]")
    private List<String> avoidIngredients;

    @Schema(
            description = "Ingredients the member is allergic to.",
            example = "[\"peanut\",\"shrimp\"]")
    private List<String> allergyIngredients;

    @Schema(description = "Preferred spicy level.", example = "MILD")
    private SpicyLevel spicyLevel;

    @Schema(description = "Preferred sweetness level.", example = "LIGHT")
    private SweetLevel sweetLevel;

    @Schema(description = "Preferred oil level.", example = "LIGHT")
    private OilLevel oilLevel;

    @Schema(description = "Preferred saltiness level.", example = "LIGHT")
    private SaltLevel saltLevel;

    @Schema(
            description = "Structured nutrition goals for the member.",
            example = "{\"protein\":\"high\",\"calorie\":\"controlled\"}")
    private Map<String, Object> nutritionGoal;

    @Schema(
            description = "Additional custom rules for meal planning.",
            example = "{\"breakfast\":\"warm_only\"}")
    private Map<String, Object> extraRule;
}
