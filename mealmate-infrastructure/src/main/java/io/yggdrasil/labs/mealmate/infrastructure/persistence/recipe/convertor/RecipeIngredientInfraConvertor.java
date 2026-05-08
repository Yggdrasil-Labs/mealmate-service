package io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.IngredientType;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeIngredientDO;

@Mapper(componentModel = "spring")
public interface RecipeIngredientInfraConvertor {

    @Mapping(
            target = "ingredientType",
            source = "ingredientType",
            qualifiedByName = "toIngredientType")
    RecipeIngredient toEntity(RecipeIngredientDO dataObject);

    @Mapping(
            target = "ingredientType",
            source = "ingredientType",
            qualifiedByName = "fromIngredientType")
    RecipeIngredientDO toDo(RecipeIngredient entity);

    @Named("toIngredientType")
    default IngredientType toIngredientType(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return IngredientType.valueOf(code);
    }

    @Named("fromIngredientType")
    default String fromIngredientType(IngredientType value) {
        return value == null ? null : value.name();
    }
}
