package io.yggdrasil.labs.mealmate.app.recipe.executor;

import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.recipe.convertor.RecipeConvertor;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.UpdateRecipeCmd;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.service.RecipeDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateRecipeCmdExe {

    private final RecipeRepository recipeRepository;
    private final RecipeDomainService recipeDomainService;
    private final RecipeConvertor recipeConvertor;

    @Transactional(rollbackFor = Exception.class)
    public void execute(UpdateRecipeCmd cmd) {
        Recipe persistedRecipe = getExistingRecipe(cmd.getRecipeId());
        recipeDomainService.assertRecipeEditable(persistedRecipe);

        String recipeName = normalizeName(cmd.getName());
        recipeRepository
                .findByName(recipeName)
                .filter(recipe -> !Objects.equals(recipe.getId(), cmd.getRecipeId()))
                .ifPresent(
                        recipe -> {
                            throw new IllegalArgumentException("Recipe name already exists");
                        });

        Recipe recipe = recipeConvertor.toRecipe(cmd);
        recipe.setName(recipeName);
        recipe.setSourceType(persistedRecipe.getSourceType());
        recipe.setStatus(persistedRecipe.getStatus());
        recipe.setTasteTags(recipeDomainService.normalizeTasteTags(recipe.getTasteTags()));
        recipeDomainService.applyBabyFriendlyRule(recipe);
        recipeRepository.update(recipe);
    }

    private Recipe getExistingRecipe(Long recipeId) {
        return recipeRepository
                .findById(recipeId)
                .orElseThrow(() -> new IllegalArgumentException("Recipe does not exist"));
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }
}
