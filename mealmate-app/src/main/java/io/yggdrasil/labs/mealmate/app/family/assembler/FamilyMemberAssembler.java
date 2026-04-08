package io.yggdrasil.labs.mealmate.app.family.assembler;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyMemberCO;
import io.yggdrasil.labs.mealmate.app.family.dto.co.FamilyProfileCO;
import io.yggdrasil.labs.mealmate.app.family.dto.co.MemberPreferenceCO;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyProfile;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;

@Mapper(componentModel = "spring")
public interface FamilyMemberAssembler {

    FamilyProfileCO toFamilyProfileCO(FamilyProfile familyProfile);

    @Mapping(target = "preference", ignore = true)
    FamilyMemberCO toFamilyMemberCO(FamilyMember familyMember);

    List<FamilyMemberCO> toFamilyMemberCOList(List<FamilyMember> familyMembers);

    MemberPreferenceCO toMemberPreferenceCO(MemberPreference memberPreference);

    default FamilyMemberCO toDetailCO(
            FamilyMember familyMember, MemberPreference memberPreference) {
        FamilyMemberCO familyMemberCO = toFamilyMemberCO(familyMember);
        if (familyMemberCO == null) {
            return null;
        }
        familyMemberCO.setPreference(toMemberPreferenceCO(memberPreference));
        return familyMemberCO;
    }
}
