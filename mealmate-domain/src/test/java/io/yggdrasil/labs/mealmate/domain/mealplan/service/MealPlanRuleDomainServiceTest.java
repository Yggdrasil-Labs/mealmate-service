package io.yggdrasil.labs.mealmate.domain.mealplan.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;

import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealPlanCrowdType;

class MealPlanRuleDomainServiceTest {

    private final MealPlanRuleDomainService service = new MealPlanRuleDomainService();

    @Test
    void shouldRejectDuplicateInSameCrowd() {
        List<MealPlanItem> items =
                List.of(
                        MealPlanItem.builder()
                                .id(1L)
                                .recipeId(200L)
                                .crowdType(MealPlanCrowdType.FAMILY)
                                .build(),
                        MealPlanItem.builder()
                                .id(2L)
                                .recipeId(100L)
                                .crowdType(MealPlanCrowdType.FAMILY)
                                .build());
        assertThrows(
                IllegalArgumentException.class, () -> service.validateNoDuplicate(items, 2L, 200L));
    }

    @Test
    void shouldAllowSameRecipeForDifferentCrowd() {
        List<MealPlanItem> items =
                List.of(
                        MealPlanItem.builder()
                                .id(1L)
                                .recipeId(200L)
                                .crowdType(MealPlanCrowdType.FAMILY)
                                .build(),
                        MealPlanItem.builder()
                                .id(2L)
                                .recipeId(100L)
                                .crowdType(MealPlanCrowdType.BABY)
                                .build());
        assertDoesNotThrow(() -> service.validateNoDuplicate(items, 2L, 200L));
    }

    @Test
    void shouldExcludeSelfWhenValidating() {
        List<MealPlanItem> items =
                List.of(
                        MealPlanItem.builder()
                                .id(1L)
                                .recipeId(200L)
                                .crowdType(MealPlanCrowdType.FAMILY)
                                .build());
        assertDoesNotThrow(() -> service.validateNoDuplicate(items, 1L, 200L));
    }

    @Test
    void shouldGetUsedRecipeIds() {
        List<MealPlanItem> items =
                List.of(
                        MealPlanItem.builder()
                                .id(1L)
                                .recipeId(100L)
                                .crowdType(MealPlanCrowdType.FAMILY)
                                .build(),
                        MealPlanItem.builder()
                                .id(2L)
                                .recipeId(200L)
                                .crowdType(MealPlanCrowdType.FAMILY)
                                .build(),
                        MealPlanItem.builder()
                                .id(3L)
                                .recipeId(100L)
                                .crowdType(MealPlanCrowdType.BABY)
                                .build());
        Set<Long> usedIds = service.getUsedRecipeIds(items);
        assertEquals(Set.of(100L, 200L), usedIds);
    }
}
