package io.yggdrasil.labs.mealmate.infrastructure.persistence.family.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import io.yggdrasil.labs.mealmate.domain.family.model.MemberPreference;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.OilLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SaltLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SpicyLevel;
import io.yggdrasil.labs.mealmate.domain.family.model.enums.SweetLevel;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.family.dataobject.MemberPreferenceDO;

@Mapper(componentModel = "spring", uses = FamilyInfraMapping.class)
public interface MemberPreferenceInfraConvertor {

    @Mapping(target = "tasteTags", source = "tasteTags", qualifiedByName = "commaToList")
    @Mapping(
            target = "avoidIngredients",
            source = "avoidIngredients",
            qualifiedByName = "commaToList")
    @Mapping(
            target = "allergyIngredients",
            source = "allergyIngredients",
            qualifiedByName = "commaToList")
    @Mapping(target = "nutritionGoal", source = "nutritionGoalJson", qualifiedByName = "jsonToMap")
    @Mapping(target = "extraRule", source = "extraRuleJson", qualifiedByName = "jsonToMap")
    @Mapping(target = "spicyLevel", source = "spicyLevel", qualifiedByName = "toSpicyLevel")
    @Mapping(target = "sweetLevel", source = "sweetLevel", qualifiedByName = "toSweetLevel")
    @Mapping(target = "oilLevel", source = "oilLevel", qualifiedByName = "toOilLevel")
    @Mapping(target = "saltLevel", source = "saltLevel", qualifiedByName = "toSaltLevel")
    MemberPreference toEntity(MemberPreferenceDO dataObject);

    @Mapping(target = "tasteTags", source = "tasteTags", qualifiedByName = "listToComma")
    @Mapping(
            target = "avoidIngredients",
            source = "avoidIngredients",
            qualifiedByName = "listToComma")
    @Mapping(
            target = "allergyIngredients",
            source = "allergyIngredients",
            qualifiedByName = "listToComma")
    @Mapping(target = "nutritionGoalJson", source = "nutritionGoal", qualifiedByName = "mapToJson")
    @Mapping(target = "extraRuleJson", source = "extraRule", qualifiedByName = "mapToJson")
    @Mapping(target = "spicyLevel", source = "spicyLevel", qualifiedByName = "fromSpicyLevel")
    @Mapping(target = "sweetLevel", source = "sweetLevel", qualifiedByName = "fromSweetLevel")
    @Mapping(target = "oilLevel", source = "oilLevel", qualifiedByName = "fromOilLevel")
    @Mapping(target = "saltLevel", source = "saltLevel", qualifiedByName = "fromSaltLevel")
    MemberPreferenceDO toDo(MemberPreference entity);

    @Named("toSpicyLevel")
    default SpicyLevel toSpicyLevel(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return SpicyLevel.valueOf(code);
    }

    @Named("fromSpicyLevel")
    default String fromSpicyLevel(SpicyLevel level) {
        return level == null ? null : level.name();
    }

    @Named("toSweetLevel")
    default SweetLevel toSweetLevel(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return SweetLevel.valueOf(code);
    }

    @Named("fromSweetLevel")
    default String fromSweetLevel(SweetLevel level) {
        return level == null ? null : level.name();
    }

    @Named("toOilLevel")
    default OilLevel toOilLevel(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return OilLevel.valueOf(code);
    }

    @Named("fromOilLevel")
    default String fromOilLevel(OilLevel level) {
        return level == null ? null : level.name();
    }

    @Named("toSaltLevel")
    default SaltLevel toSaltLevel(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return SaltLevel.valueOf(code);
    }

    @Named("fromSaltLevel")
    default String fromSaltLevel(SaltLevel level) {
        return level == null ? null : level.name();
    }
}
