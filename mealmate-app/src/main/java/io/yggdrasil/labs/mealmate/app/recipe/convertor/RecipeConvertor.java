package io.yggdrasil.labs.mealmate.app.recipe.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.CreateRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.NutritionFactCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.RecipeIngredientItemCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.RecipeStepItemCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeCmd;
import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;

@Mapper(componentModel = "spring")
public interface RecipeConvertor {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "sourceType", ignore = true),
        @Mapping(target = "status", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    Recipe toRecipe(CreateRecipeCmd cmd);

    @Mappings({
        @Mapping(target = "id", source = "recipeId"),
        @Mapping(target = "sourceType", ignore = true),
        @Mapping(target = "status", ignore = true),
        @Mapping(target = "ingredients", ignore = true),
        @Mapping(target = "steps", ignore = true),
        @Mapping(target = "nutritionFact", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true),
        @Mapping(target = "deleted", ignore = true)
    })
    Recipe toRecipe(UpdateRecipeCmd cmd);

    List<RecipeIngredient> toRecipeIngredients(List<RecipeIngredientItemCmd> items);

    List<RecipeStep> toRecipeSteps(List<RecipeStepItemCmd> items);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "recipeId", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true)
    })
    NutritionFact toNutritionFact(NutritionFactCmd cmd);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "recipeId", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true)
    })
    RecipeIngredient toRecipeIngredient(RecipeIngredientItemCmd item);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "recipeId", ignore = true),
        @Mapping(target = "createdAt", ignore = true),
        @Mapping(target = "updatedAt", ignore = true),
        @Mapping(target = "createdBy", ignore = true),
        @Mapping(target = "updatedBy", ignore = true)
    })
    RecipeStep toRecipeStep(RecipeStepItemCmd item);

    default NutritionFact toNutritionFact(NutritionFactCmd cmd, Long recipeId) {
        NutritionFact nutritionFact = toNutritionFact(cmd);
        if (nutritionFact != null) {
            nutritionFact.setRecipeId(recipeId);
        }
        return nutritionFact;
    }
}
