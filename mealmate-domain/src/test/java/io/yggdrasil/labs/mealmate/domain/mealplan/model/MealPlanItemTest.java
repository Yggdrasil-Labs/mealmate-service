package io.yggdrasil.labs.mealmate.domain.mealplan.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MealPlanItemTest {

    @Test
    void adjustShouldUpdateRecipeAndIncrementCount() {
        MealPlanItem item = MealPlanItem.builder().id(1L).recipeId(100L).build();
        item.adjust(200L);
        assertEquals(200L, item.getRecipeId());
        assertTrue(item.isManuallyAdjusted());
        assertEquals(1, item.getAdjustCount());
    }

    @Test
    void adjustTwiceShouldIncrementToTwo() {
        MealPlanItem item = MealPlanItem.builder().id(1L).recipeId(100L).build();
        item.adjust(200L);
        item.adjust(300L);
        assertEquals(300L, item.getRecipeId());
        assertEquals(2, item.getAdjustCount());
        assertTrue(item.isManuallyAdjusted());
    }
}
