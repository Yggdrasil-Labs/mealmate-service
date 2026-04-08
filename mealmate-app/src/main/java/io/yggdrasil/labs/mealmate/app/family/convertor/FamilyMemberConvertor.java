package io.yggdrasil.labs.mealmate.app.family.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import io.yggdrasil.labs.mealmate.app.family.dto.cmd.AddFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.UpdateFamilyMemberCmd;
import io.yggdrasil.labs.mealmate.app.family.dto.cmd.UpdateMemberPreferenceCmd;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;

@Mapper(componentModel = "spring")
public interface FamilyMemberConvertor {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    FamilyMember toFamilyMember(AddFamilyMemberCmd cmd);

    @Mappings({
        @Mapping(target = "id", source = "memberId"),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    FamilyMember toFamilyMember(UpdateFamilyMemberCmd cmd);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true)
    })
    MemberPreference toMemberPreference(UpdateMemberPreferenceCmd cmd);
}
