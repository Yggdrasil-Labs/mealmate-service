package io.yggdrasil.labs.mealmate.app.family.dto.cmd;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.yggdrasil.labs.mealmate.domain.family.model.enums.GenderType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberStatus;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberTargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFamilyMemberCmd {

    @NotNull private Long familyId;

    @NotNull private Long memberId;

    @NotBlank private String name;

    @NotNull private MemberRoleType roleType;

    private GenderType gender;
    private LocalDate birthday;
    private String region;
    private MemberTargetType targetType;
    private String avatarUrl;
    private Integer sortNo;
    private MemberStatus status;
}
