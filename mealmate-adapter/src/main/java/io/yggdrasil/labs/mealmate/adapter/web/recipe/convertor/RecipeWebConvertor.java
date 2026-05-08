package io.yggdrasil.labs.mealmate.adapter.web.recipe.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.CreateRecipeRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipeDetailResponse;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipePageRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipeResponse;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.RecipeSearchRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeIngredientsRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeNutritionRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeStatusRequest;
import io.yggdrasil.labs.mealmate.adapter.web.recipe.dto.UpdateRecipeStepsRequest;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.CreateRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.DeleteRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeIngredientsCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeNutritionCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeStatusCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeStepsCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeDetailCO;
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.GetRecipeDetailQry;
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.PageRecipeQry;
import io.yggdrasil.labs.mealmate.app.recipe.dto.qry.SearchRecipeQry;

@Mapper(componentModel = "spring")
public interface RecipeWebConvertor {

    CreateRecipeCmd toCreateRecipeCmd(CreateRecipeRequest request);

    @Mapping(target = "recipeId", source = "recipeId")
    UpdateRecipeCmd toUpdateRecipeCmd(Long recipeId, UpdateRecipeRequest request);

    @Mapping(target = "recipeId", source = "recipeId")
    UpdateRecipeIngredientsCmd toUpdateRecipeIngredientsCmd(
            Long recipeId, UpdateRecipeIngredientsRequest request);

    @Mapping(target = "recipeId", source = "recipeId")
    UpdateRecipeStepsCmd toUpdateRecipeStepsCmd(Long recipeId, UpdateRecipeStepsRequest request);

    @Mapping(target = "recipeId", source = "recipeId")
    UpdateRecipeNutritionCmd toUpdateRecipeNutritionCmd(
            Long recipeId, UpdateRecipeNutritionRequest request);

    @Mapping(target = "recipeId", source = "recipeId")
    UpdateRecipeStatusCmd toUpdateRecipeStatusCmd(Long recipeId, UpdateRecipeStatusRequest request);

    @Mapping(target = "babyFriendly", source = "isBabyFriendly")
    @Mapping(target = "weightLossFriendly", source = "isWeightLossFriendly")
    PageRecipeQry toPageRecipeQry(RecipePageRequest request);

    SearchRecipeQry toSearchRecipeQry(RecipeSearchRequest request);

    default GetRecipeDetailQry toGetRecipeDetailQry(Long recipeId) {
        return new GetRecipeDetailQry(recipeId);
    }

    default DeleteRecipeCmd toDeleteRecipeCmd(Long recipeId) {
        return new DeleteRecipeCmd(recipeId);
    }

    RecipeResponse toRecipeResponse(RecipeCO recipeCO);

    List<RecipeResponse> toRecipeResponseList(List<RecipeCO> recipeCOList);

    RecipeDetailResponse toRecipeDetailResponse(RecipeDetailCO recipeDetailCO);
}
