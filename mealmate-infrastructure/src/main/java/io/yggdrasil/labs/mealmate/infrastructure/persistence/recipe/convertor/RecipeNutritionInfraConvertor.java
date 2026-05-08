package io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeNutritionDO;

@Mapper(componentModel = "spring", uses = RecipeInfraMapping.class)
public interface RecipeNutritionInfraConvertor {

    @Mapping(target = "nutritionJson", source = "nutritionJson", qualifiedByName = "jsonToMap")
    NutritionFact toEntity(RecipeNutritionDO dataObject);

    @Mapping(target = "nutritionJson", source = "nutritionJson", qualifiedByName = "mapToJson")
    RecipeNutritionDO toDo(NutritionFact entity);
}
