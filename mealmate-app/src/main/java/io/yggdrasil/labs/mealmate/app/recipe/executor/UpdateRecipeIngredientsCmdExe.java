package io.yggdrasil.labs.mealmate.app.recipe.executor;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.recipe.convertor.RecipeConvertor;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeIngredientsCmd;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.service.RecipeDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateRecipeIngredientsCmdExe {

    private final RecipeRepository recipeRepository;
    private final RecipeDomainService recipeDomainService;
    private final RecipeConvertor recipeConvertor;

    @Transactional(rollbackFor = Exception.class)
    public void execute(UpdateRecipeIngredientsCmd cmd) {
        Recipe persistedRecipe = getExistingRecipe(cmd.getRecipeId());
        recipeDomainService.assertRecipeEditable(persistedRecipe);

        List<RecipeIngredient> ingredients =
                recipeDomainService.normalizeIngredients(
                        recipeConvertor.toRecipeIngredients(cmd.getIngredients()));
        recipeRepository.updateIngredients(cmd.getRecipeId(), ingredients);
    }

    private Recipe getExistingRecipe(Long recipeId) {
        return recipeRepository
                .findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
    }
}
