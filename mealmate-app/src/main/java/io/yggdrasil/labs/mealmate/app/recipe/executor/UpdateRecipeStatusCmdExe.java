package io.yggdrasil.labs.mealmate.app.recipe.executor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeStatusCmd;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.service.RecipeDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateRecipeStatusCmdExe {

    private final RecipeRepository recipeRepository;
    private final RecipeDomainService recipeDomainService;

    @Transactional(rollbackFor = Exception.class)
    public void execute(UpdateRecipeStatusCmd cmd) {
        Recipe persistedRecipe = getExistingRecipe(cmd.getRecipeId());
        recipeDomainService.assertRecipeEditable(persistedRecipe);
        recipeRepository.updateStatus(cmd.getRecipeId(), cmd.getStatus());
    }

    private Recipe getExistingRecipe(Long recipeId) {
        return recipeRepository
                .findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
    }
}
