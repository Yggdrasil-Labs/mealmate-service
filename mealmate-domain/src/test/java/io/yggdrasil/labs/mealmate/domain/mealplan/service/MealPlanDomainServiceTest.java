package io.yggdrasil.labs.mealmate.domain.mealplan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.PrepPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.ShoppingItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeIngredient;

/** 周计划领域服务单元测试。 */
class MealPlanDomainServiceTest {

    private final IngredientFilterDomainService filterService = new IngredientFilterDomainService();
    private final DuplicateCheckDomainService dupService = new DuplicateCheckDomainService();
    private final WeekPlanGenerateDomainService genService = new WeekPlanGenerateDomainService();
    private final PrepPlanDeriveDomainService deriveService = new PrepPlanDeriveDomainService();

    // ─── IngredientFilterDomainService ───

    @Test
    void filterShouldExcludeRecipesContainingAvoidIngredients() {
        Recipe safe = recipeWith(1L, "安全菜", "土豆");
        Recipe unsafe = recipeWith(2L, "含虾", "虾");
        Set<String> avoid = new HashSet<>(Arrays.asList("虾"));

        List<Recipe> result = filterService.filter(Arrays.asList(safe, unsafe), avoid, null);

        assertEquals(1, result.size());
        assertEquals("安全菜", result.get(0).getName());
    }

    @Test
    void filterShouldPassAllWhenNoForbiddenIngredients() {
        Recipe r1 = recipeWith(1L, "菜A", "土豆");
        Recipe r2 = recipeWith(2L, "菜B", "白菜");

        List<Recipe> result = filterService.filter(Arrays.asList(r1, r2), null, null);

        assertEquals(2, result.size());
    }

    @Test
    void filterShouldExcludeAllergyIngredients() {
        Recipe allergen = recipeWith(1L, "花生菜", "花生");
        Set<String> allergy = new HashSet<>(Arrays.asList("花生"));

        List<Recipe> result = filterService.filter(Arrays.asList(allergen), null, allergy);

        assertTrue(result.isEmpty());
    }

    // ─── DuplicateCheckDomainService ───

    @Test
    void shouldMarkDuplicateWhenSameRecipeAppearsMultipleTimes() {
        MealPlanItem i1 = MealPlanItem.builder().recipeId(1L).build();
        MealPlanItem i2 = MealPlanItem.builder().recipeId(1L).build();
        MealPlanItem i3 = MealPlanItem.builder().recipeId(2L).build();

        dupService.markDuplicates(Arrays.asList(i1, i2, i3));

        assertTrue(i1.isDuplicateFlag());
        assertTrue(i2.isDuplicateFlag());
        assertFalse(i3.isDuplicateFlag());
    }

    @Test
    void shouldNotMarkDuplicateWhenAllRecipesDistinct() {
        MealPlanItem i1 = MealPlanItem.builder().recipeId(1L).build();
        MealPlanItem i2 = MealPlanItem.builder().recipeId(2L).build();

        dupService.markDuplicates(Arrays.asList(i1, i2));

        assertFalse(i1.isDuplicateFlag());
        assertFalse(i2.isDuplicateFlag());
    }

    // ─── WeekPlanGenerateDomainService ───

    @Test
    void shouldGenerate35ItemsFor7Days5MealsPerDay() {
        List<Recipe> candidates = buildCandidates(30);
        LocalDate monday = LocalDate.of(2026, 5, 25);

        WeeklyMealPlan plan = genService.generate(1L, monday, candidates);

        assertEquals(35, plan.getItems().size());
        assertEquals(PlanStatus.DRAFT, plan.getStatus());
        assertEquals(monday, plan.getWeekStartDate());
        assertEquals(monday.plusDays(6), plan.getWeekEndDate());
    }

    @Test
    void shouldThrowWhenCandidatesEmpty() {
        assertThrows(
                IllegalStateException.class,
                () -> genService.generate(1L, LocalDate.now(), Collections.emptyList()));
    }

    @Test
    void shouldHandleFewCandidatesWithDegradation() {
        // 只有 5 个候选，不够 35 不重样，应降级循环使用
        List<Recipe> fewCandidates = buildCandidates(5);
        LocalDate monday = LocalDate.of(2026, 5, 25);

        WeeklyMealPlan plan = genService.generate(1L, monday, fewCandidates);

        assertEquals(35, plan.getItems().size());
    }

    // ─── PrepPlanDeriveDomainService ───

    @Test
    void shouldMergeIngredientsAcrossItems() {
        Recipe r1 = recipeWith(1L, "菜A", "土豆", new BigDecimal("200"), "g");
        Recipe r2 = recipeWith(2L, "菜B", "土豆", new BigDecimal("100"), "g");
        Map<Long, Recipe> map = new HashMap<>();
        map.put(1L, r1);
        map.put(2L, r2);
        List<MealPlanItem> items =
                Arrays.asList(
                        MealPlanItem.builder().recipeId(1L).build(),
                        MealPlanItem.builder().recipeId(2L).build());

        PrepPlan prep = deriveService.derivePrepPlan(1L, items, map);

        assertEquals(1, prep.getItems().size());
        assertEquals(new BigDecimal("300"), prep.getItems().get(0).getQuantity());
    }

    @Test
    void shouldDeriveShoppingListWithMergedQuantities() {
        Recipe r1 = recipeWith(1L, "菜A", "白菜", new BigDecimal("500"), "g");
        Recipe r2 = recipeWith(2L, "菜B", "白菜", new BigDecimal("300"), "g");
        Map<Long, Recipe> map = new HashMap<>();
        map.put(1L, r1);
        map.put(2L, r2);
        List<MealPlanItem> items =
                Arrays.asList(
                        MealPlanItem.builder().recipeId(1L).build(),
                        MealPlanItem.builder().recipeId(2L).build());

        List<ShoppingItem> list = deriveService.deriveShoppingList(1L, items, map);

        assertEquals(1, list.size());
        assertEquals(new BigDecimal("800"), list.get(0).getTotalQuantity());
    }

    // ─── Helpers ───

    private Recipe recipeWith(Long id, String name, String ingredientName) {
        return recipeWith(id, name, ingredientName, null, null);
    }

    private Recipe recipeWith(
            Long id, String name, String ingredientName, BigDecimal qty, String unit) {
        Recipe r = new Recipe();
        r.setId(id);
        r.setName(name);
        RecipeIngredient ing = new RecipeIngredient();
        ing.setIngredientName(ingredientName);
        ing.setQuantity(qty);
        ing.setUnit(unit);
        r.setIngredients(Collections.singletonList(ing));
        return r;
    }

    private List<Recipe> buildCandidates(int count) {
        List<Recipe> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Recipe r = new Recipe();
            r.setId((long) i);
            r.setName("菜品" + i);
            r.setCookingTimeMin(i % 3 == 0 ? 15 : 30);
            r.setBabyFriendly(i % 2 == 0);
            r.setWeightLossFriendly(i % 3 == 0);
            list.add(r);
        }
        return list;
    }
}
