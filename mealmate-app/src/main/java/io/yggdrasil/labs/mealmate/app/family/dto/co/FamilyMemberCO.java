package io.yggdrasil.labs.mealmate.app.family.dto.co;

import java.time.LocalDate;

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
public class FamilyMemberCO {

    private Long id;
    private Long familyId;
    private String name;
    private MemberRoleType roleType;
    private GenderType gender;
    private LocalDate birthday;
    private String region;
    private MemberTargetType targetType;
    private String avatarUrl;
    private Integer sortNo;
    private MemberStatus status;
    private MemberPreferenceCO preference;
}
