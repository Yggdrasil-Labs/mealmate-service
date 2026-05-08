package io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import io.yggdrasil.labs.mealmate.domain.recipe.model.NutritionFact;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeStep;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.IngredientType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeSourceType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.impl.RecipeRepositoryImpl;
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

@ExtendWith(MockitoExtension.class)
class RecipeRepositoryImplTest {

    @Mock private RecipeInfraConvertor recipeInfraConvertor;
    @Mock private RecipeIngredientInfraConvertor recipeIngredientInfraConvertor;
    @Mock private RecipeStepInfraConvertor recipeStepInfraConvertor;
    @Mock private RecipeNutritionInfraConvertor recipeNutritionInfraConvertor;
    @Mock private RecipeService recipeService;
    @Mock private RecipeIngredientService recipeIngredientService;
    @Mock private RecipeStepService recipeStepService;
    @Mock private RecipeNutritionService recipeNutritionService;

    @InjectMocks private RecipeRepositoryImpl recipeRepository;

    @Test
    void shouldFindRecipeByIdWithChildrenAssembled() {
        RecipeDO recipeDO = recipeDO(1L, "Tomato Egg");
        RecipeIngredientDO ingredientDO1 = ingredientDO(11L, 1L, "Tomato", 1);
        RecipeIngredientDO ingredientDO2 = ingredientDO(12L, 1L, "Egg", 2);
        RecipeStepDO stepDO = stepDO(21L, 1L, 1, "Mix and cook");
        RecipeNutritionDO nutritionDO = nutritionDO(31L, 1L, BigDecimal.valueOf(100));

        when(recipeService.getById(1L)).thenReturn(recipeDO);
        when(recipeIngredientService.list(anyIngredientQuery()))
                .thenReturn(List.of(ingredientDO1, ingredientDO2));
        when(recipeStepService.list(anyStepQuery())).thenReturn(List.of(stepDO));
        when(recipeNutritionService.getOne(anyNutritionQuery())).thenReturn(nutritionDO);
        when(recipeInfraConvertor.toEntity(recipeDO)).thenReturn(recipeEntity(1L, "Tomato Egg"));
        when(recipeIngredientInfraConvertor.toEntity(ingredientDO1))
                .thenReturn(ingredientEntity(11L, 1L, "Tomato", 1));
        when(recipeIngredientInfraConvertor.toEntity(ingredientDO2))
                .thenReturn(ingredientEntity(12L, 1L, "Egg", 2));
        when(recipeStepInfraConvertor.toEntity(stepDO))
                .thenReturn(stepEntity(21L, 1L, 1, "Mix and cook"));
        when(recipeNutritionInfraConvertor.toEntity(nutritionDO))
                .thenReturn(nutritionEntity(31L, 1L, BigDecimal.valueOf(100)));

        Optional<Recipe> recipe = recipeRepository.findById(1L);

        assertTrue(recipe.isPresent());
        assertEquals(1L, recipe.get().getId());
        assertEquals("Tomato Egg", recipe.get().getName());
        assertEquals(2, recipe.get().getIngredients().size());
        assertEquals(1, recipe.get().getSteps().size());
        assertEquals(31L, recipe.get().getNutritionFact().getId());
    }

    @Test
    void shouldPageRecipesWithFiltersAndOrder() {
        RecipeDO recipeDO = recipeDO(1L, "Winter Soup");
        Page<RecipeDO> page = new Page<>(2, 20);
        page.setRecords(List.of(recipeDO));

        when(recipeService.page(any(), any())).thenReturn(page);
        when(recipeInfraConvertor.toEntity(recipeDO)).thenReturn(recipeEntity(1L, "Winter Soup"));

        List<Recipe> recipes =
                recipeRepository.page(
                        "Soup",
                        RecipeType.SOUP,
                        SeasonTag.WINTER,
                        CrowdTag.BABY,
                        true,
                        false,
                        DifficultyLevel.EASY,
                        30,
                        2,
                        20);

        assertEquals(1, recipes.size());
        assertEquals("Winter Soup", recipes.get(0).getName());

        ArgumentCaptor<Page<RecipeDO>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<LambdaQueryWrapper<RecipeDO>> queryCaptor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(recipeService).page(pageCaptor.capture(), queryCaptor.capture());
        assertEquals(2L, pageCaptor.getValue().getCurrent());
        assertEquals(20L, pageCaptor.getValue().getSize());

        assertTrue(queryCaptor.getValue() != null);
    }

    @Test
    void shouldSearchRecipesByKeyword() {
        RecipeDO recipeDO = recipeDO(2L, "Pumpkin Porridge");
        Page<RecipeDO> page = new Page<>(1, 5);
        page.setRecords(List.of(recipeDO));

        when(recipeService.page(any(), any())).thenReturn(page);
        when(recipeInfraConvertor.toEntity(recipeDO))
                .thenReturn(recipeEntity(2L, "Pumpkin Porridge"));

        List<Recipe> recipes = recipeRepository.searchByKeyword("Pumpkin", 5);

        assertEquals(1, recipes.size());
        assertEquals("Pumpkin Porridge", recipes.get(0).getName());

        ArgumentCaptor<Page<RecipeDO>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        ArgumentCaptor<LambdaQueryWrapper<RecipeDO>> queryCaptor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(recipeService).page(pageCaptor.capture(), queryCaptor.capture());
        assertEquals(1L, pageCaptor.getValue().getCurrent());
        assertEquals(5L, pageCaptor.getValue().getSize());
        assertTrue(queryCaptor.getValue() != null);
    }

    @Test
    void shouldSaveRecipeWithChildren() {
        Recipe recipe = recipeEntity(1L, "Tomato Egg");
        recipe.setIngredients(
                List.of(
                        ingredientEntity(null, null, "Tomato", 2),
                        ingredientEntity(null, null, "Egg", 1)));
        recipe.setSteps(
                List.of(stepEntity(null, null, 2, "Cook"), stepEntity(null, null, 1, "Mix")));
        recipe.setNutritionFact(nutritionEntity(null, null, BigDecimal.valueOf(100)));

        RecipeDO recipeDO = recipeDO(null, "Tomato Egg");
        when(recipeInfraConvertor.toDo(recipe)).thenReturn(recipeDO);
        doAnswer(
                        invocation -> {
                            RecipeDO argument = invocation.getArgument(0);
                            argument.setId(10L);
                            return true;
                        })
                .when(recipeService)
                .save(any(RecipeDO.class));
        doAnswer(
                        invocation -> {
                            Collection<RecipeIngredientDO> dataObjects = invocation.getArgument(0);
                            long id = 101L;
                            for (RecipeIngredientDO dataObject : dataObjects) {
                                dataObject.setId(id++);
                            }
                            return true;
                        })
                .when(recipeIngredientService)
                .saveBatch(any());
        doAnswer(
                        invocation -> {
                            Collection<RecipeStepDO> dataObjects = invocation.getArgument(0);
                            long id = 201L;
                            for (RecipeStepDO dataObject : dataObjects) {
                                dataObject.setId(id++);
                            }
                            return true;
                        })
                .when(recipeStepService)
                .saveBatch(any());
        doAnswer(
                        invocation -> {
                            RecipeNutritionDO dataObject = invocation.getArgument(0);
                            dataObject.setId(301L);
                            return true;
                        })
                .when(recipeNutritionService)
                .save(any(RecipeNutritionDO.class));
        when(recipeInfraConvertor.toEntity(recipeDO))
                .thenAnswer(
                        invocation -> {
                            RecipeDO argument = invocation.getArgument(0);
                            Recipe saved = recipeEntity(argument.getId(), argument.getName());
                            return saved;
                        });
        when(recipeIngredientInfraConvertor.toDo(any()))
                .thenAnswer(
                        invocation -> {
                            RecipeIngredient entity = invocation.getArgument(0);
                            RecipeIngredientDO dataObject = new RecipeIngredientDO();
                            dataObject.setRecipeId(entity.getRecipeId());
                            dataObject.setIngredientName(entity.getIngredientName());
                            dataObject.setSortNo(entity.getSortNo());
                            return dataObject;
                        });
        when(recipeStepInfraConvertor.toDo(any()))
                .thenAnswer(
                        invocation -> {
                            RecipeStep entity = invocation.getArgument(0);
                            RecipeStepDO dataObject = new RecipeStepDO();
                            dataObject.setRecipeId(entity.getRecipeId());
                            dataObject.setStepNo(entity.getStepNo());
                            dataObject.setContent(entity.getContent());
                            return dataObject;
                        });
        when(recipeNutritionInfraConvertor.toDo(any()))
                .thenAnswer(
                        invocation -> {
                            NutritionFact entity = invocation.getArgument(0);
                            RecipeNutritionDO dataObject = new RecipeNutritionDO();
                            dataObject.setRecipeId(entity.getRecipeId());
                            dataObject.setCalories(entity.getCalories());
                            return dataObject;
                        });
        when(recipeIngredientInfraConvertor.toEntity(any()))
                .thenAnswer(
                        invocation -> {
                            RecipeIngredientDO dataObject = invocation.getArgument(0);
                            return ingredientEntity(
                                    dataObject.getId(),
                                    dataObject.getRecipeId(),
                                    dataObject.getIngredientName(),
                                    dataObject.getSortNo());
                        });
        when(recipeStepInfraConvertor.toEntity(any()))
                .thenAnswer(
                        invocation -> {
                            RecipeStepDO dataObject = invocation.getArgument(0);
                            return stepEntity(
                                    dataObject.getId(),
                                    dataObject.getRecipeId(),
                                    dataObject.getStepNo(),
                                    dataObject.getContent());
                        });
        when(recipeNutritionInfraConvertor.toEntity(any()))
                .thenAnswer(
                        invocation -> {
                            RecipeNutritionDO dataObject = invocation.getArgument(0);
                            return nutritionEntity(
                                    dataObject.getId(),
                                    dataObject.getRecipeId(),
                                    dataObject.getCalories());
                        });

        Recipe saved = recipeRepository.save(recipe);

        assertEquals(10L, saved.getId());
        assertEquals("Tomato Egg", saved.getName());
        assertEquals(101L, saved.getIngredients().get(0).getId());
        assertEquals(102L, saved.getIngredients().get(1).getId());
        assertEquals(201L, saved.getSteps().get(0).getId());
        assertEquals(202L, saved.getSteps().get(1).getId());
        assertEquals(301L, saved.getNutritionFact().getId());

        ArgumentCaptor<Collection<RecipeIngredientDO>> ingredientCaptor =
                ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<Collection<RecipeStepDO>> stepCaptor =
                ArgumentCaptor.forClass(Collection.class);
        verify(recipeIngredientService).saveBatch(ingredientCaptor.capture());
        verify(recipeStepService).saveBatch(stepCaptor.capture());
        verify(recipeNutritionService).save(any(RecipeNutritionDO.class));
        assertEquals(2, ingredientCaptor.getValue().size());
        assertEquals(2, stepCaptor.getValue().size());
        assertTrue(
                ingredientCaptor.getValue().stream()
                        .allMatch(item -> item.getRecipeId().equals(10L)));
        assertTrue(stepCaptor.getValue().stream().allMatch(item -> item.getRecipeId().equals(10L)));
    }

    @Test
    void shouldMarkDeletedRecipeWithRecipeIdInsteadOfBinaryFlag() {
        recipeRepository.logicalDeleteById(9L);

        verify(recipeService).update(anyRecipeUpdate());
    }

    @Test
    void shouldReplaceIngredientsByRecipeId() {
        List<RecipeIngredient> ingredients =
                List.of(
                        ingredientEntity(null, null, "Tomato", 2),
                        ingredientEntity(null, null, "Egg", 1));
        when(recipeIngredientInfraConvertor.toDo(any()))
                .thenAnswer(
                        invocation -> {
                            RecipeIngredient entity = invocation.getArgument(0);
                            RecipeIngredientDO dataObject = new RecipeIngredientDO();
                            dataObject.setRecipeId(entity.getRecipeId());
                            dataObject.setIngredientName(entity.getIngredientName());
                            dataObject.setSortNo(entity.getSortNo());
                            return dataObject;
                        });

        recipeRepository.updateIngredients(1L, ingredients);

        verify(recipeIngredientService).remove(anyIngredientQuery());
        ArgumentCaptor<Collection<RecipeIngredientDO>> ingredientCaptor =
                ArgumentCaptor.forClass(Collection.class);
        verify(recipeIngredientService).saveBatch(ingredientCaptor.capture());
        assertEquals(2, ingredientCaptor.getValue().size());
        assertTrue(
                ingredientCaptor.getValue().stream()
                        .allMatch(item -> item.getRecipeId().equals(1L)));
    }

    @Test
    void shouldReplaceStepsByRecipeId() {
        List<RecipeStep> steps =
                List.of(stepEntity(null, null, 2, "Cook"), stepEntity(null, null, 1, "Mix"));
        when(recipeStepInfraConvertor.toDo(any()))
                .thenAnswer(
                        invocation -> {
                            RecipeStep entity = invocation.getArgument(0);
                            RecipeStepDO dataObject = new RecipeStepDO();
                            dataObject.setRecipeId(entity.getRecipeId());
                            dataObject.setStepNo(entity.getStepNo());
                            dataObject.setContent(entity.getContent());
                            return dataObject;
                        });

        recipeRepository.updateSteps(1L, steps);

        verify(recipeStepService).remove(anyStepQuery());
        ArgumentCaptor<Collection<RecipeStepDO>> stepCaptor =
                ArgumentCaptor.forClass(Collection.class);
        verify(recipeStepService).saveBatch(stepCaptor.capture());
        assertEquals(2, stepCaptor.getValue().size());
        assertTrue(stepCaptor.getValue().stream().allMatch(item -> item.getRecipeId().equals(1L)));
    }

    @Test
    void shouldInsertNutritionWhenMissing() {
        NutritionFact nutritionFact = nutritionEntity(null, null, BigDecimal.valueOf(123));
        when(recipeNutritionService.getOne(anyNutritionQuery())).thenReturn(null);
        when(recipeNutritionInfraConvertor.toDo(any()))
                .thenAnswer(
                        invocation -> {
                            NutritionFact entity = invocation.getArgument(0);
                            RecipeNutritionDO dataObject = new RecipeNutritionDO();
                            dataObject.setRecipeId(entity.getRecipeId());
                            dataObject.setCalories(entity.getCalories());
                            return dataObject;
                        });

        recipeRepository.updateNutrition(1L, nutritionFact);

        ArgumentCaptor<RecipeNutritionDO> nutritionCaptor =
                ArgumentCaptor.forClass(RecipeNutritionDO.class);
        verify(recipeNutritionService).save(nutritionCaptor.capture());
        assertEquals(1L, nutritionCaptor.getValue().getRecipeId());
        assertEquals(BigDecimal.valueOf(123), nutritionCaptor.getValue().getCalories());
    }

    @Test
    void shouldUpdateNutritionWhenPresent() {
        NutritionFact nutritionFact = nutritionEntity(null, null, BigDecimal.valueOf(123));
        RecipeNutritionDO existing = nutritionDO(99L, 1L, BigDecimal.valueOf(50));
        when(recipeNutritionService.getOne(anyNutritionQuery())).thenReturn(existing);
        when(recipeNutritionInfraConvertor.toDo(any()))
                .thenAnswer(
                        invocation -> {
                            NutritionFact entity = invocation.getArgument(0);
                            RecipeNutritionDO dataObject = new RecipeNutritionDO();
                            dataObject.setRecipeId(entity.getRecipeId());
                            dataObject.setCalories(entity.getCalories());
                            return dataObject;
                        });

        recipeRepository.updateNutrition(1L, nutritionFact);

        ArgumentCaptor<RecipeNutritionDO> nutritionCaptor =
                ArgumentCaptor.forClass(RecipeNutritionDO.class);
        verify(recipeNutritionService).updateById(nutritionCaptor.capture());
        assertEquals(99L, nutritionCaptor.getValue().getId());
        assertEquals(1L, nutritionCaptor.getValue().getRecipeId());
        assertEquals(BigDecimal.valueOf(123), nutritionCaptor.getValue().getCalories());
    }

    @Test
    void shouldLogicallyDeleteRecipeAndChildren() {
        recipeRepository.logicalDeleteById(1L);

        verify(recipeService).update(anyRecipeUpdate());
        verify(recipeIngredientService).remove(anyIngredientQuery());
        verify(recipeStepService).remove(anyStepQuery());
        verify(recipeNutritionService).remove(anyNutritionQuery());
    }

    private LambdaQueryWrapper<RecipeIngredientDO> anyIngredientQuery() {
        return any();
    }

    private UpdateWrapper<RecipeDO> anyRecipeUpdate() {
        return any();
    }

    private LambdaQueryWrapper<RecipeStepDO> anyStepQuery() {
        return any();
    }

    private LambdaQueryWrapper<RecipeNutritionDO> anyNutritionQuery() {
        return any();
    }

    private RecipeDO recipeDO(Long id, String name) {
        RecipeDO dataObject = new RecipeDO();
        dataObject.setId(id);
        dataObject.setName(name);
        dataObject.setRecipeType(RecipeType.SOUP.name());
        dataObject.setSourceType(RecipeSourceType.MANUAL.name());
        dataObject.setSeasonTag(SeasonTag.WINTER.name());
        dataObject.setCrowdTag(CrowdTag.GENERAL.name());
        dataObject.setDifficultyLevel(DifficultyLevel.EASY.name());
        dataObject.setStatus(RecipeStatus.ACTIVE.name());
        return dataObject;
    }

    private Recipe recipeEntity(Long id, String name) {
        Recipe recipe = new Recipe();
        recipe.setId(id);
        recipe.setName(name);
        recipe.setRecipeType(RecipeType.SOUP);
        recipe.setSourceType(RecipeSourceType.MANUAL);
        recipe.setSeasonTag(SeasonTag.WINTER);
        recipe.setCrowdTag(CrowdTag.GENERAL);
        recipe.setDifficultyLevel(DifficultyLevel.EASY);
        recipe.setStatus(RecipeStatus.ACTIVE);
        return recipe;
    }

    private RecipeIngredientDO ingredientDO(Long id, Long recipeId, String name, Integer sortNo) {
        RecipeIngredientDO dataObject = new RecipeIngredientDO();
        dataObject.setId(id);
        dataObject.setRecipeId(recipeId);
        dataObject.setIngredientName(name);
        dataObject.setSortNo(sortNo);
        dataObject.setIngredientType(IngredientType.VEGETABLE.name());
        return dataObject;
    }

    private RecipeIngredient ingredientEntity(Long id, Long recipeId, String name, Integer sortNo) {
        RecipeIngredient ingredient = new RecipeIngredient();
        ingredient.setId(id);
        ingredient.setRecipeId(recipeId);
        ingredient.setIngredientName(name);
        ingredient.setSortNo(sortNo);
        ingredient.setIngredientType(IngredientType.VEGETABLE);
        ingredient.setQuantity(BigDecimal.ONE);
        ingredient.setUnit("g");
        ingredient.setMainIngredient(true);
        return ingredient;
    }

    private RecipeStepDO stepDO(Long id, Long recipeId, Integer stepNo, String content) {
        RecipeStepDO dataObject = new RecipeStepDO();
        dataObject.setId(id);
        dataObject.setRecipeId(recipeId);
        dataObject.setStepNo(stepNo);
        dataObject.setContent(content);
        return dataObject;
    }

    private RecipeStep stepEntity(Long id, Long recipeId, Integer stepNo, String content) {
        RecipeStep step = new RecipeStep();
        step.setId(id);
        step.setRecipeId(recipeId);
        step.setStepNo(stepNo);
        step.setContent(content);
        return step;
    }

    private RecipeNutritionDO nutritionDO(Long id, Long recipeId, BigDecimal calories) {
        RecipeNutritionDO dataObject = new RecipeNutritionDO();
        dataObject.setId(id);
        dataObject.setRecipeId(recipeId);
        dataObject.setCalories(calories);
        return dataObject;
    }

    private NutritionFact nutritionEntity(Long id, Long recipeId, BigDecimal calories) {
        NutritionFact nutritionFact = new NutritionFact();
        nutritionFact.setId(id);
        nutritionFact.setRecipeId(recipeId);
        nutritionFact.setCalories(calories);
        return nutritionFact;
    }
}
