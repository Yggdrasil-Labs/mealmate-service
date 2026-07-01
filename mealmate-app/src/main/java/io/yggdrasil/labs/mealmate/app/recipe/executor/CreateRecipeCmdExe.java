package io.yggdrasil.labs.mealmate.app.recipe.executor;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.recipe.assembler.RecipeAssembler;
import io.yggdrasil.labs.mealmate.app.recipe.convertor.RecipeConvertor;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.CreateRecipeCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.RecipeDetailCO;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.domain.recipe.service.RecipeDomainService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CreateRecipeCmdExe {

    private final RecipeRepository recipeRepository;
    private final RecipeDomainService recipeDomainService;
    private final RecipeConvertor recipeConvertor;
    private final RecipeAssembler recipeAssembler;

    @Transactional(rollbackFor = Exception.class)
    public RecipeDetailCO execute(CreateRecipeCmd cmd) {
        String recipeName = normalizeName(cmd.getName());
        recipeRepository
                .findByName(recipeName)
                .ifPresent(
                        recipe -> {
                            throw new IllegalArgumentException("Recipe name already exists");
                        });

        Recipe recipe = recipeConvertor.toRecipe(cmd);
        recipe.setName(recipeName);
        recipe.setSourceType(
                cmd.getSourceType() != null ? cmd.getSourceType() : RecipeSourceType.MANUAL);
        if (recipe.getStatus() == null) {
            recipe.setStatus(RecipeStatus.ACTIVE);
        }
        recipe.setTasteTags(recipeDomainService.normalizeTasteTags(recipe.getTasteTags()));
        recipe.setIngredients(recipeDomainService.normalizeIngredients(recipe.getIngredients()));
        recipe.setSteps(recipeDomainService.normalizeSteps(recipe.getSteps()));
        recipeDomainService.applyBabyFriendlyRule(recipe);
        recipeDomainService.validateNutritionFact(recipe.getNutritionFact());

        Recipe savedRecipe = recipeRepository.save(recipe);
        return recipeAssembler.toRecipeDetailCO(savedRecipe);
    }

    private String normalizeName(String name) {
        return name == null ? null : name.trim();
    }
}
