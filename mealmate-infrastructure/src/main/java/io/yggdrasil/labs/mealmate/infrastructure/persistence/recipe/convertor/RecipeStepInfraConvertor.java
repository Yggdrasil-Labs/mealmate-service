package io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor;

import org.mapstruct.Mapper;

import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeStepDO;

@Mapper(componentModel = "spring")
public interface RecipeStepInfraConvertor {

    RecipeStep toEntity(RecipeStepDO dataObject);

    RecipeStepDO toDo(RecipeStep entity);
}
