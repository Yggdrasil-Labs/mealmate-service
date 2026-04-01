package io.yggdrasil.labs.mealmate.infrastructure.persistence.family.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import io.yggdrasil.labs.mealmate.domain.family.model.FamilyProfile;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.FamilyStatus;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.FamilyProfileDO;

@Mapper(componentModel = "spring", uses = FamilyInfraMapping.class)
public interface FamilyProfileInfraConvertor {

    @Mapping(target = "mealGoal", source = "mealGoalJson", qualifiedByName = "jsonToMap")
    @Mapping(target = "status", source = "status", qualifiedByName = "toFamilyStatus")
    FamilyProfile toEntity(FamilyProfileDO dataObject);

    @Mapping(target = "mealGoalJson", source = "mealGoal", qualifiedByName = "mapToJson")
    @Mapping(target = "status", source = "status", qualifiedByName = "fromFamilyStatus")
    FamilyProfileDO toDo(FamilyProfile entity);

    @Named("toFamilyStatus")
    default FamilyStatus toFamilyStatus(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return FamilyStatus.valueOf(code);
    }

    @Named("fromFamilyStatus")
    default String fromFamilyStatus(FamilyStatus status) {
        return status == null ? null : status.name();
    }
}
