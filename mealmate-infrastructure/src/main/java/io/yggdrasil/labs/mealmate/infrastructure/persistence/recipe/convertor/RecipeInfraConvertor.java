package io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeDO;

@Mapper(componentModel = "spring", uses = RecipeInfraMapping.class)
public interface RecipeInfraConvertor {

    @Mapping(target = "tasteTags", source = "tasteTag", qualifiedByName = "commaToList")
    @Mapping(target = "recipeType", source = "recipeType", qualifiedByName = "toRecipeType")
    @Mapping(target = "sourceType", source = "sourceType", qualifiedByName = "toRecipeSourceType")
    @Mapping(target = "seasonTag", source = "seasonTag", qualifiedByName = "toSeasonTag")
    @Mapping(target = "crowdTag", source = "crowdTag", qualifiedByName = "toCrowdTag")
    @Mapping(
            target = "difficultyLevel",
            source = "difficultyLevel",
            qualifiedByName = "toDifficultyLevel")
    @Mapping(target = "status", source = "status", qualifiedByName = "toRecipeStatus")
    Recipe toEntity(RecipeDO dataObject);

    @Mapping(target = "tasteTag", source = "tasteTags", qualifiedByName = "listToComma")
    @Mapping(target = "recipeType", source = "recipeType", qualifiedByName = "fromRecipeType")
    @Mapping(target = "sourceType", source = "sourceType", qualifiedByName = "fromRecipeSourceType")
    @Mapping(target = "seasonTag", source = "seasonTag", qualifiedByName = "fromSeasonTag")
    @Mapping(target = "crowdTag", source = "crowdTag", qualifiedByName = "fromCrowdTag")
    @Mapping(
            target = "difficultyLevel",
            source = "difficultyLevel",
            qualifiedByName = "fromDifficultyLevel")
    @Mapping(target = "status", source = "status", qualifiedByName = "fromRecipeStatus")
    RecipeDO toDo(Recipe entity);

    @Named("toRecipeType")
    default RecipeType toRecipeType(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return RecipeType.valueOf(code);
    }

    @Named("fromRecipeType")
    default String fromRecipeType(RecipeType value) {
        return value == null ? null : value.name();
    }

    @Named("toRecipeSourceType")
    default RecipeSourceType toRecipeSourceType(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return RecipeSourceType.valueOf(code);
    }

    @Named("fromRecipeSourceType")
    default String fromRecipeSourceType(RecipeSourceType value) {
        return value == null ? null : value.name();
    }

    @Named("toSeasonTag")
    default SeasonTag toSeasonTag(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return SeasonTag.valueOf(code);
    }

    @Named("fromSeasonTag")
    default String fromSeasonTag(SeasonTag value) {
        return value == null ? null : value.name();
    }

    @Named("toCrowdTag")
    default CrowdTag toCrowdTag(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return CrowdTag.valueOf(code);
    }

    @Named("fromCrowdTag")
    default String fromCrowdTag(CrowdTag value) {
        return value == null ? null : value.name();
    }

    @Named("toDifficultyLevel")
    default DifficultyLevel toDifficultyLevel(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return DifficultyLevel.valueOf(code);
    }

    @Named("fromDifficultyLevel")
    default String fromDifficultyLevel(DifficultyLevel value) {
        return value == null ? null : value.name();
    }

    @Named("toRecipeStatus")
    default RecipeStatus toRecipeStatus(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return RecipeStatus.valueOf(code);
    }

    @Named("fromRecipeStatus")
    default String fromRecipeStatus(RecipeStatus value) {
        return value == null ? null : value.name();
    }
}
