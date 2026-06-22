package io.yggdrasil.labs.mealmate.infrastructure.persistence.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeQueryCriteria;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeRepository;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor.RecipeInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor.RecipeIngredientInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor.RecipeNutritionInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor.RecipeStepInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeIngredientDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeNutritionDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeStepDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.service.RecipeIngredientService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.service.RecipeNutritionService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.service.RecipeService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.service.RecipeStepService;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RecipeRepositoryImpl implements RecipeRepository {

    private static final long DEFAULT_PAGE_NUM = 1L;
    private static final long DEFAULT_PAGE_SIZE = 10L;

    private final RecipeInfraConvertor recipeInfraConvertor;
    private final RecipeIngredientInfraConvertor recipeIngredientInfraConvertor;
    private final RecipeStepInfraConvertor recipeStepInfraConvertor;
    private final RecipeNutritionInfraConvertor recipeNutritionInfraConvertor;
    private final RecipeService recipeService;
    private final RecipeIngredientService recipeIngredientService;
    private final RecipeStepService recipeStepService;
    private final RecipeNutritionService recipeNutritionService;

    @Override
    public Optional<Recipe> findById(Long recipeId) {
        if (recipeId == null) {
            return Optional.empty();
        }
        RecipeDO recipeDO = recipeService.getById(recipeId);
        if (recipeDO == null) {
            return Optional.empty();
        }
        Recipe recipe = recipeInfraConvertor.toEntity(recipeDO);
        recipe.setIngredients(findIngredients(recipeId));
        recipe.setSteps(findSteps(recipeId));
        recipe.setNutritionFact(findNutrition(recipeId).orElse(null));
        return Optional.of(recipe);
    }

    @Override
    public List<Recipe> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<RecipeDO> dataObjects = recipeService.listByIds(ids);
        if (dataObjects == null || dataObjects.isEmpty()) {
            return Collections.emptyList();
        }
        return dataObjects.stream()
                .map(recipeInfraConvertor::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Recipe> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        LambdaQueryWrapper<RecipeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RecipeDO::getName, name);
        return Optional.ofNullable(recipeService.getOne(queryWrapper))
                .map(recipeInfraConvertor::toEntity);
    }

    @Override
    public List<Recipe> page(RecipeQueryCriteria criteria) {
        Page<RecipeDO> page =
                new Page<>(
                        normalizePageNum(criteria.getPageNum()),
                        normalizePageSize(criteria.getPageSize()));
        LambdaQueryWrapper<RecipeDO> queryWrapper = buildRecipeQuery(criteria);
        queryWrapper.orderByDesc(RecipeDO::getUpdatedAt, RecipeDO::getId);
        Page<RecipeDO> result = recipeService.page(page, queryWrapper);
        if (result == null || result.getRecords() == null || result.getRecords().isEmpty()) {
            return Collections.emptyList();
        }
        return result.getRecords().stream()
                .map(recipeInfraConvertor::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int count(RecipeQueryCriteria criteria) {
        LambdaQueryWrapper<RecipeDO> queryWrapper = buildRecipeQuery(criteria);
        return Math.toIntExact(recipeService.count(queryWrapper));
    }

    @Override
    public List<Recipe> searchByKeyword(String keyword, Integer limit) {
        Page<RecipeDO> page = new Page<>(DEFAULT_PAGE_NUM, normalizePageSize(limit));
        LambdaQueryWrapper<RecipeDO> queryWrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            queryWrapper.like(RecipeDO::getName, keyword.trim());
        }
        queryWrapper.orderByDesc(RecipeDO::getUpdatedAt, RecipeDO::getId);
        Page<RecipeDO> result = recipeService.page(page, queryWrapper);
        if (result == null || result.getRecords() == null || result.getRecords().isEmpty()) {
            return Collections.emptyList();
        }
        return result.getRecords().stream()
                .map(recipeInfraConvertor::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Recipe save(Recipe recipe) {
        if (recipe == null) {
            return null;
        }
        RecipeDO recipeDO = recipeInfraConvertor.toDo(recipe);
        recipeService.save(recipeDO);
        Long recipeId = recipeDO.getId();
        List<RecipeIngredientDO> savedIngredients =
                saveIngredients(recipeId, recipe.getIngredients());
        List<RecipeStepDO> savedSteps = saveSteps(recipeId, recipe.getSteps());
        RecipeNutritionDO savedNutrition = saveNutrition(recipeId, recipe.getNutritionFact());
        Recipe savedRecipe = recipeInfraConvertor.toEntity(recipeDO);
        savedRecipe.setIngredients(returnIngredients(savedIngredients));
        savedRecipe.setSteps(returnSteps(savedSteps));
        savedRecipe.setNutritionFact(returnNutrition(savedNutrition));
        return savedRecipe;
    }

    @Override
    public void update(Recipe recipe) {
        if (recipe == null) {
            return;
        }
        recipeService.updateById(recipeInfraConvertor.toDo(recipe));
    }

    @Override
    public void updateIngredients(Long recipeId, List<RecipeIngredient> ingredients) {
        if (recipeId == null) {
            return;
        }
        LambdaQueryWrapper<RecipeIngredientDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RecipeIngredientDO::getRecipeId, recipeId);
        recipeIngredientService.remove(queryWrapper);
        saveIngredients(recipeId, ingredients);
    }

    @Override
    public void updateSteps(Long recipeId, List<RecipeStep> steps) {
        if (recipeId == null) {
            return;
        }
        LambdaQueryWrapper<RecipeStepDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RecipeStepDO::getRecipeId, recipeId);
        recipeStepService.remove(queryWrapper);
        saveSteps(recipeId, steps);
    }

    @Override
    public void updateNutrition(Long recipeId, NutritionFact nutritionFact) {
        if (recipeId == null || nutritionFact == null) {
            return;
        }
        nutritionFact.setRecipeId(recipeId);
        LambdaQueryWrapper<RecipeNutritionDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RecipeNutritionDO::getRecipeId, recipeId);
        RecipeNutritionDO existing = recipeNutritionService.getOne(queryWrapper);
        RecipeNutritionDO dataObject = recipeNutritionInfraConvertor.toDo(nutritionFact);
        if (existing == null) {
            recipeNutritionService.save(dataObject);
            return;
        }
        dataObject.setId(existing.getId());
        recipeNutritionService.updateById(dataObject);
    }

    @Override
    public void updateStatus(Long recipeId, RecipeStatus status) {
        if (recipeId == null || status == null) {
            return;
        }
        RecipeDO dataObject = new RecipeDO();
        dataObject.setId(recipeId);
        dataObject.setStatus(status.name());
        recipeService.updateById(dataObject);
    }

    @Override
    public void logicalDeleteById(Long recipeId) {
        if (recipeId == null) {
            return;
        }
        LambdaQueryWrapper<RecipeIngredientDO> ingredientQuery = new LambdaQueryWrapper<>();
        ingredientQuery.eq(RecipeIngredientDO::getRecipeId, recipeId);
        recipeIngredientService.remove(ingredientQuery);

        LambdaQueryWrapper<RecipeStepDO> stepQuery = new LambdaQueryWrapper<>();
        stepQuery.eq(RecipeStepDO::getRecipeId, recipeId);
        recipeStepService.remove(stepQuery);

        LambdaQueryWrapper<RecipeNutritionDO> nutritionQuery = new LambdaQueryWrapper<>();
        nutritionQuery.eq(RecipeNutritionDO::getRecipeId, recipeId);
        recipeNutritionService.remove(nutritionQuery);

        UpdateWrapper<RecipeDO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", recipeId);
        updateWrapper.eq("deleted", 0L);
        updateWrapper.set("deleted", recipeId);
        recipeService.update(updateWrapper);
    }

    private List<RecipeIngredient> findIngredients(Long recipeId) {
        LambdaQueryWrapper<RecipeIngredientDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RecipeIngredientDO::getRecipeId, recipeId);
        queryWrapper.orderByAsc(RecipeIngredientDO::getSortNo, RecipeIngredientDO::getId);
        List<RecipeIngredientDO> dataObjects = recipeIngredientService.list(queryWrapper);
        if (dataObjects == null || dataObjects.isEmpty()) {
            return Collections.emptyList();
        }
        return dataObjects.stream()
                .map(recipeIngredientInfraConvertor::toEntity)
                .collect(Collectors.toList());
    }

    private List<RecipeStep> findSteps(Long recipeId) {
        LambdaQueryWrapper<RecipeStepDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RecipeStepDO::getRecipeId, recipeId);
        queryWrapper.orderByAsc(RecipeStepDO::getStepNo, RecipeStepDO::getId);
        List<RecipeStepDO> dataObjects = recipeStepService.list(queryWrapper);
        if (dataObjects == null || dataObjects.isEmpty()) {
            return Collections.emptyList();
        }
        return dataObjects.stream()
                .map(recipeStepInfraConvertor::toEntity)
                .collect(Collectors.toList());
    }

    private Optional<NutritionFact> findNutrition(Long recipeId) {
        LambdaQueryWrapper<RecipeNutritionDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RecipeNutritionDO::getRecipeId, recipeId);
        return Optional.ofNullable(recipeNutritionService.getOne(queryWrapper))
                .map(recipeNutritionInfraConvertor::toEntity);
    }

    private LambdaQueryWrapper<RecipeDO> buildRecipeQuery(RecipeQueryCriteria criteria) {
        LambdaQueryWrapper<RecipeDO> queryWrapper = new LambdaQueryWrapper<>();
        if (criteria.getKeyword() != null && !criteria.getKeyword().isBlank()) {
            queryWrapper.like(RecipeDO::getName, criteria.getKeyword().trim());
        }
        if (criteria.getRecipeType() != null) {
            queryWrapper.eq(RecipeDO::getRecipeType, criteria.getRecipeType().name());
        }
        if (criteria.getSeasonTag() != null) {
            queryWrapper.eq(RecipeDO::getSeasonTag, criteria.getSeasonTag().name());
        }
        if (criteria.getCrowdTag() != null) {
            queryWrapper.eq(RecipeDO::getCrowdTag, criteria.getCrowdTag().name());
        }
        if (criteria.getBabyFriendly() != null) {
            queryWrapper.eq(RecipeDO::getBabyFriendly, criteria.getBabyFriendly());
        }
        if (criteria.getWeightLossFriendly() != null) {
            queryWrapper.eq(RecipeDO::getWeightLossFriendly, criteria.getWeightLossFriendly());
        }
        if (criteria.getDifficultyLevel() != null) {
            queryWrapper.eq(RecipeDO::getDifficultyLevel, criteria.getDifficultyLevel().name());
        }
        if (criteria.getMaxCookingTime() != null) {
            queryWrapper.le(RecipeDO::getCookingTimeMin, criteria.getMaxCookingTime());
        }
        return queryWrapper;
    }

    private List<RecipeIngredientDO> saveIngredients(
            Long recipeId, List<RecipeIngredient> ingredients) {
        if (recipeId == null || ingredients == null || ingredients.isEmpty()) {
            return Collections.emptyList();
        }
        List<RecipeIngredientDO> dataObjects = new ArrayList<>(ingredients.size());
        for (RecipeIngredient ingredient : ingredients) {
            ingredient.setRecipeId(recipeId);
            dataObjects.add(recipeIngredientInfraConvertor.toDo(ingredient));
        }
        recipeIngredientService.saveBatch(dataObjects);
        return dataObjects;
    }

    private List<RecipeStepDO> saveSteps(Long recipeId, List<RecipeStep> steps) {
        if (recipeId == null || steps == null || steps.isEmpty()) {
            return Collections.emptyList();
        }
        List<RecipeStepDO> dataObjects = new ArrayList<>(steps.size());
        for (RecipeStep step : steps) {
            step.setRecipeId(recipeId);
            dataObjects.add(recipeStepInfraConvertor.toDo(step));
        }
        recipeStepService.saveBatch(dataObjects);
        return dataObjects;
    }

    private RecipeNutritionDO saveNutrition(Long recipeId, NutritionFact nutritionFact) {
        if (recipeId == null || nutritionFact == null) {
            return null;
        }
        nutritionFact.setRecipeId(recipeId);
        RecipeNutritionDO dataObject = recipeNutritionInfraConvertor.toDo(nutritionFact);
        recipeNutritionService.save(dataObject);
        return dataObject;
    }

    private List<RecipeIngredient> returnIngredients(List<RecipeIngredientDO> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return Collections.emptyList();
        }
        return ingredients.stream()
                .map(recipeIngredientInfraConvertor::toEntity)
                .collect(Collectors.toList());
    }

    private List<RecipeStep> returnSteps(List<RecipeStepDO> steps) {
        if (steps == null || steps.isEmpty()) {
            return Collections.emptyList();
        }
        return steps.stream().map(recipeStepInfraConvertor::toEntity).collect(Collectors.toList());
    }

    private NutritionFact returnNutrition(RecipeNutritionDO nutritionFact) {
        if (nutritionFact == null) {
            return null;
        }
        return recipeNutritionInfraConvertor.toEntity(nutritionFact);
    }

    private long normalizePageNum(Integer pageNum) {
        return pageNum == null || pageNum <= 0 ? DEFAULT_PAGE_NUM : pageNum;
    }

    private long normalizePageSize(Integer pageSize) {
        return pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
    }
}
