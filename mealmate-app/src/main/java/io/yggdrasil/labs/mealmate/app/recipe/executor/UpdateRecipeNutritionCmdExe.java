package io.yggdrasil.labs.mealmate.app.recipe.executor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.recipe.convertor.RecipeConvertor;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeNutritionCmd;
import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.service.RecipeDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateRecipeNutritionCmdExe {

    private final RecipeRepository recipeRepository;
    private final RecipeDomainService recipeDomainService;
    private final RecipeConvertor recipeConvertor;

    @Transactional(rollbackFor = Exception.class)
    public void execute(UpdateRecipeNutritionCmd cmd) {
        Recipe persistedRecipe = getExistingRecipe(cmd.getRecipeId());
        recipeDomainService.assertRecipeEditable(persistedRecipe);

        NutritionFact nutritionFact = recipeConvertor.toNutritionFact(cmd.getNutritionFact());
        recipeDomainService.validateNutritionFact(nutritionFact);
        recipeRepository.updateNutrition(cmd.getRecipeId(), nutritionFact);
    }

    private Recipe getExistingRecipe(Long recipeId) {
        return recipeRepository
                .findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
    }
}
