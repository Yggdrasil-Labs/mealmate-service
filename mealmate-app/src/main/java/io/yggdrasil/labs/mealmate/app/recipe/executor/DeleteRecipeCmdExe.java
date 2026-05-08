package io.yggdrasil.labs.mealmate.app.recipe.executor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.DeleteRecipeCmd;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.service.RecipeDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeleteRecipeCmdExe {

    private final RecipeRepository recipeRepository;
    private final RecipeDomainService recipeDomainService;

    @Transactional(rollbackFor = Exception.class)
    public void execute(DeleteRecipeCmd cmd) {
        Recipe persistedRecipe = getExistingRecipe(cmd.getRecipeId());
        recipeDomainService.assertRecipeDeletable(persistedRecipe);
        recipeRepository.logicalDeleteById(cmd.getRecipeId());
    }

    private Recipe getExistingRecipe(Long recipeId) {
        return recipeRepository
                .findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
    }
}
