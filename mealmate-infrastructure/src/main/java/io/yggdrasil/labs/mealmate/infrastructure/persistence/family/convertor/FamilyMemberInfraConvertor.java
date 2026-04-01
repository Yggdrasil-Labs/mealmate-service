package io.yggdrasil.labs.mealmate.infrastructure.persistence.family.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import io.yggdrasil.labs.mealmate.domain.family.model.FamilyMember;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.GenderType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberRoleType;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberStatus;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.MemberTargetType;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.FamilyMemberDO;

@Mapper(componentModel = "spring")
public interface FamilyMemberInfraConvertor {

    @Mapping(target = "roleType", source = "roleType", qualifiedByName = "toMemberRoleType")
    @Mapping(target = "gender", source = "gender", qualifiedByName = "toGenderType")
    @Mapping(target = "targetType", source = "targetType", qualifiedByName = "toMemberTargetType")
    @Mapping(target = "status", source = "status", qualifiedByName = "toMemberStatus")
    FamilyMember toEntity(FamilyMemberDO dataObject);

    @Mapping(target = "roleType", source = "roleType", qualifiedByName = "fromMemberRoleType")
    @Mapping(target = "gender", source = "gender", qualifiedByName = "fromGenderType")
    @Mapping(target = "targetType", source = "targetType", qualifiedByName = "fromMemberTargetType")
    @Mapping(target = "status", source = "status", qualifiedByName = "fromMemberStatus")
    FamilyMemberDO toDo(FamilyMember entity);

    @Named("toMemberRoleType")
    default MemberRoleType toMemberRoleType(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return MemberRoleType.valueOf(code);
    }

    @Named("fromMemberRoleType")
    default String fromMemberRoleType(MemberRoleType value) {
        return value == null ? null : value.name();
    }

    @Named("toGenderType")
    default GenderType toGenderType(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return GenderType.valueOf(code);
    }

    @Named("fromGenderType")
    default String fromGenderType(GenderType value) {
        return value == null ? null : value.name();
    }

    @Named("toMemberTargetType")
    default MemberTargetType toMemberTargetType(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return MemberTargetType.valueOf(code);
    }

    @Named("fromMemberTargetType")
    default String fromMemberTargetType(MemberTargetType value) {
        return value == null ? null : value.name();
    }

    @Named("toMemberStatus")
    default MemberStatus toMemberStatus(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return MemberStatus.valueOf(code);
    }

    @Named("fromMemberStatus")
    default String fromMemberStatus(MemberStatus value) {
        return value == null ? null : value.name();
    }
}
