package io.yggdrasil.labs.mealmate.app.family.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import io.yggdrasil.labs.mealmate.app.family.dto.cmd.CreateFamilyCmd;
import io.yggdrasil.labs.mealmate.domain.family.model.FamilyProfile;

@Mapper(componentModel = "spring")
public interface FamilyProfileConvertor {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "familyCode", ignore = true),
        @Mapping(target = "status", ignore = true),
        @Mapping(target = "region", ignore = true),
        @Mapping(target = "mealGoal", ignore = true),
        @Mapping(target = "remark", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    FamilyProfile toFamilyProfile(CreateFamilyCmd cmd);
}
