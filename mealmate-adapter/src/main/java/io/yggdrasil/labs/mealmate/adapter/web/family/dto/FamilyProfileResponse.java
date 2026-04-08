package io.yggdrasil.labs.mealmate.adapter.web.family.dto;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums.FamilyStatus;
import lombok.Data;

@Data
@Schema(description = "Response model for the profile of a family.")
public class FamilyProfileResponse {

    @Schema(description = "Unique identifier of the family.", example = "1001")
    private Long id;

    @Schema(description = "Display name of the family.", example = "Yang Family")
    private String familyName;

    @Schema(description = "Stable business code of the family.", example = "FAM-001")
    private String familyCode;

    @Schema(description = "Lifecycle status of the family.", example = "ENABLED")
    private FamilyStatus status;

    @Schema(description = "Region associated with the family.", example = "Shanghai")
    private String region;

    @Schema(
            description = "Structured meal planning goals shared by the family.",
            example = "{\"weekday\":\"quick\",\"weekend\":\"balanced\"}")
    private Map<String, Object> mealGoal;

    @Schema(
            description = "Additional remarks about the family.",
            example = "Need baby-friendly food.")
    private String remark;
}
