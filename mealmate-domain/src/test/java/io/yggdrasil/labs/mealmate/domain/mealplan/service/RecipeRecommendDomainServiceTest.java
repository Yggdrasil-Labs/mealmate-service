package io.yggdrasil.labs.mealmate.domain.mealplan.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;

import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;

class RecipeRecommendDomainServiceTest {

    private final RecipeRecommendDomainService service = new RecipeRecommendDomainService();

    @Test
    void shouldExcludeUsedRecipes() {
        List<Recipe> candidates =
                List.of(
                        Recipe.builder().id(1L).name("A").build(),
                        Recipe.builder().id(2L).name("B").build(),
                        Recipe.builder().id(3L).name("C").build());
        Set<Long> usedIds = Set.of(1L, 3L);
        List<Recipe> result = service.recommend(candidates, usedIds, 20);
        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void shouldLimitResults() {
        List<Recipe> candidates = new ArrayList<>();
        for (long i = 1; i <= 30; i++) {
            candidates.add(Recipe.builder().id(i).name("R" + i).build());
        }
        List<Recipe> result = service.recommend(candidates, Set.of(), 20);
        assertEquals(20, result.size());
    }
}
