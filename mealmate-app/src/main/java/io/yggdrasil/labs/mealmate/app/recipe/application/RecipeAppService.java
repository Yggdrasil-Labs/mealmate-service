package io.yggdrasil.labs.mealmate.app.recipe.application;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

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
import io.yggdrasil.labs.mealmate.app.recipe.executor.CountRecipeQryExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.CreateRecipeCmdExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.DeleteRecipeCmdExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.GetRecipeDetailQryExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.PageRecipeQryExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.SearchRecipeQryExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.UpdateRecipeCmdExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.UpdateRecipeIngredientsCmdExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.UpdateRecipeNutritionCmdExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.UpdateRecipeStatusCmdExe;
import io.yggdrasil.labs.mealmate.app.recipe.executor.UpdateRecipeStepsCmdExe;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class RecipeAppService {

    private final CreateRecipeCmdExe createRecipeCmdExe;
    private final UpdateRecipeCmdExe updateRecipeCmdExe;
    private final UpdateRecipeIngredientsCmdExe updateRecipeIngredientsCmdExe;
    private final UpdateRecipeStepsCmdExe updateRecipeStepsCmdExe;
    private final UpdateRecipeNutritionCmdExe updateRecipeNutritionCmdExe;
    private final UpdateRecipeStatusCmdExe updateRecipeStatusCmdExe;
    private final DeleteRecipeCmdExe deleteRecipeCmdExe;
    private final PageRecipeQryExe pageRecipeQryExe;
    private final CountRecipeQryExe countRecipeQryExe;
    private final GetRecipeDetailQryExe getRecipeDetailQryExe;
    private final SearchRecipeQryExe searchRecipeQryExe;

    public RecipeDetailCO createRecipe(@Valid CreateRecipeCmd cmd) {
        return createRecipeCmdExe.execute(cmd);
    }

    public void updateRecipe(@Valid UpdateRecipeCmd cmd) {
        updateRecipeCmdExe.execute(cmd);
    }

    public void updateRecipeIngredients(@Valid UpdateRecipeIngredientsCmd cmd) {
        updateRecipeIngredientsCmdExe.execute(cmd);
    }

    public void updateRecipeSteps(@Valid UpdateRecipeStepsCmd cmd) {
        updateRecipeStepsCmdExe.execute(cmd);
    }

    public void updateRecipeNutrition(@Valid UpdateRecipeNutritionCmd cmd) {
        updateRecipeNutritionCmdExe.execute(cmd);
    }

    public void updateRecipeStatus(@Valid UpdateRecipeStatusCmd cmd) {
        updateRecipeStatusCmdExe.execute(cmd);
    }

    public void deleteRecipe(@Valid DeleteRecipeCmd cmd) {
        deleteRecipeCmdExe.execute(cmd);
    }

    public List<RecipeCO> pageRecipe(@Valid PageRecipeQry qry) {
        return pageRecipeQryExe.execute(qry);
    }

    public int countRecipe(@Valid PageRecipeQry qry) {
        return countRecipeQryExe.execute(qry);
    }

    public RecipeDetailCO getRecipeDetail(@Valid GetRecipeDetailQry qry) {
        return getRecipeDetailQryExe.execute(qry);
    }

    public List<RecipeCO> searchRecipe(@Valid SearchRecipeQry qry) {
        return searchRecipeQryExe.execute(qry);
    }
}
