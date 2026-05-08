package io.yggdrasil.labs.mealmate.app.recipe.executor;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.recipe.convertor.RecipeConvertor;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeStepsCmd;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.service.RecipeDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateRecipeStepsCmdExe {

    private final RecipeRepository recipeRepository;
    private final RecipeDomainService recipeDomainService;
    private final RecipeConvertor recipeConvertor;

    @Transactional(rollbackFor = Exception.class)
    public void execute(UpdateRecipeStepsCmd cmd) {
        Recipe persistedRecipe = getExistingRecipe(cmd.getRecipeId());
        recipeDomainService.assertRecipeEditable(persistedRecipe);

        List<RecipeStep> steps =
                recipeDomainService.normalizeSteps(recipeConvertor.toRecipeSteps(cmd.getSteps()));
        recipeRepository.updateSteps(cmd.getRecipeId(), steps);
    }

    private Recipe getExistingRecipe(Long recipeId) {
        return recipeRepository
                .findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
    }
}
