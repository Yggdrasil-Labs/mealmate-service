package io.yggdrasil.labs.mealmate.adapter.web.family.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums.GenderType;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums.MemberStatus;
import io.yggdrasil.labs.mealmate.adapter.web.family.dto.enums.MemberTargetType;
import lombok.Data;

@Data
@Schema(description = "Request payload for updating the basic profile of a family member.")
public class UpdateFamilyMemberRequest {

    @Schema(description = "Display name of the family member.", example = "Alice")
    @NotBlank
    private String name;

    @Schema(description = "Role of the member in the family.", example = "ADULT")
    @NotNull
    private MemberRoleType roleType;

    @Schema(description = "Gender of the member.", example = "FEMALE")
    private GenderType gender;

    @Schema(description = "Birthday of the member.", example = "2018-05-20")
    private LocalDate birthday;

    @Schema(description = "Region or locale associated with the member.", example = "Shanghai")
    private String region;

    @Schema(description = "Dietary or health target of the member.", example = "BALANCED")
    private MemberTargetType targetType;

    @Schema(
            description = "Avatar image URL of the member.",
            example = "https://cdn.example.com/avatar/alice.png")
    private String avatarUrl;

    @Schema(description = "Display order within the family.", example = "1")
    private Integer sortNo;

    @Schema(description = "Current lifecycle status of the member.", example = "ACTIVE")
    private MemberStatus status;
}
