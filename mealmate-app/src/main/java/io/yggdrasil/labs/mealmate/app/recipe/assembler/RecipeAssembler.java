package io.yggdrasil.labs.mealmate.app.recipe.assembler;

import java.util.List;

import org.mapstruct.Mapper;

import io.yggdrasil.labs.mealmate.app.recipe.dto.co.NutritionFactCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeDetailCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeIngredientCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeStepCO;
import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;

@Mapper(componentModel = "spring")
public interface RecipeAssembler {

    RecipeCO toRecipeCO(Recipe recipe);

    List<RecipeCO> toRecipeCOList(List<Recipe> recipes);

    RecipeDetailCO toRecipeDetailCO(Recipe recipe);

    RecipeIngredientCO toRecipeIngredientCO(RecipeIngredient ingredient);

    List<RecipeIngredientCO> toRecipeIngredientCOList(List<RecipeIngredient> ingredients);

    RecipeStepCO toRecipeStepCO(RecipeStep step);

    List<RecipeStepCO> toRecipeStepCOList(List<RecipeStep> steps);

    NutritionFactCO toNutritionFactCO(NutritionFact nutritionFact);
}
