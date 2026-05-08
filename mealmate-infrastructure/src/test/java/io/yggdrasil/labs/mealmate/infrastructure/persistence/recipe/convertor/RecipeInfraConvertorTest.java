package io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeIngredientDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeNutritionDO;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.RecipeStepDO;

@SpringJUnitConfig(
        classes = {
            RecipeInfraMapping.class,
            RecipeInfraConvertorImpl.class,
            RecipeIngredientInfraConvertorImpl.class,
            RecipeStepInfraConvertorImpl.class,
            RecipeNutritionInfraConvertorImpl.class
        })
class RecipeInfraConvertorTest {

    @Autowired private RecipeInfraConvertor recipeInfraConvertor;

    @Autowired private RecipeIngredientInfraConvertor recipeIngredientInfraConvertor;

    @Autowired private RecipeStepInfraConvertor recipeStepInfraConvertor;

    @Autowired private RecipeNutritionInfraConvertor recipeNutritionInfraConvertor;

    @Test
    void shouldConvertRecipeEnumsAndTasteTagsRoundTrip() {
        RecipeDO dataObject = new RecipeDO();
        dataObject.setRecipeType("MAIN_DISH");
        dataObject.setSourceType("AI_GENERATED");
        dataObject.setSeasonTag("SUMMER");
        dataObject.setCrowdTag("BABY");
        dataObject.setTasteTag(" light , spicy , light ");
        dataObject.setDifficultyLevel("EASY");
        dataObject.setStatus("ACTIVE");

        Recipe entity = recipeInfraConvertor.toEntity(dataObject);

        assertEquals(RecipeType.MAIN_DISH, entity.getRecipeType());
        assertEquals(RecipeSourceType.AI_GENERATED, entity.getSourceType());
        assertEquals(SeasonTag.SUMMER, entity.getSeasonTag());
        assertEquals(CrowdTag.BABY, entity.getCrowdTag());
        assertEquals(List.of("light", "spicy", "light"), entity.getTasteTags());
        assertEquals(DifficultyLevel.EASY, entity.getDifficultyLevel());
        assertEquals(RecipeStatus.ACTIVE, entity.getStatus());

        entity.setRecipeType(RecipeType.SOUP);
        entity.setSourceType(RecipeSourceType.SYSTEM);
        entity.setSeasonTag(SeasonTag.WINTER);
        entity.setCrowdTag(CrowdTag.WEIGHT_LOSS);
        entity.setTasteTags(List.of("fresh", "hot"));
        entity.setDifficultyLevel(DifficultyLevel.MEDIUM);
        entity.setStatus(RecipeStatus.INACTIVE);

        RecipeDO roundTrip = recipeInfraConvertor.toDo(entity);

        assertEquals("SOUP", roundTrip.getRecipeType());
        assertEquals("SYSTEM", roundTrip.getSourceType());
        assertEquals("WINTER", roundTrip.getSeasonTag());
        assertEquals("WEIGHT_LOSS", roundTrip.getCrowdTag());
        assertEquals("fresh,hot", roundTrip.getTasteTag());
        assertEquals("MEDIUM", roundTrip.getDifficultyLevel());
        assertEquals("INACTIVE", roundTrip.getStatus());
    }

    @Test
    void shouldConvertNutritionJsonRoundTrip() {
        RecipeNutritionDO dataObject = new RecipeNutritionDO();
        dataObject.setNutritionJson("{\"kcal\":200,\"note\":\"fresh\"}");

        NutritionFact entity = recipeNutritionInfraConvertor.toEntity(dataObject);

        assertNotNull(entity.getNutritionJson());
        assertEquals(200, ((Number) entity.getNutritionJson().get("kcal")).intValue());
        assertEquals("fresh", entity.getNutritionJson().get("note"));

        Map<String, Object> nutritionJson = new HashMap<>();
        nutritionJson.put("kcal", 320);
        nutritionJson.put("fiber", 8);

        entity.setNutritionJson(nutritionJson);

        RecipeNutritionDO roundTrip = recipeNutritionInfraConvertor.toDo(entity);

        assertNotNull(roundTrip.getNutritionJson());
        assertTrue(roundTrip.getNutritionJson().contains("kcal"));
        assertTrue(roundTrip.getNutritionJson().contains("320"));
        assertTrue(roundTrip.getNutritionJson().contains("fiber"));
    }

    @Test
    void shouldConvertQuotedNutritionJsonFromDatabase() {
        RecipeNutritionDO dataObject = new RecipeNutritionDO();
        dataObject.setNutritionJson("\"{\\\"notes\\\":\\\"balanced\\\"}\"");

        NutritionFact entity = recipeNutritionInfraConvertor.toEntity(dataObject);

        assertNotNull(entity.getNutritionJson());
        assertEquals("balanced", entity.getNutritionJson().get("notes"));
    }

    @Test
    void shouldConvertRecipeIngredientListItemRoundTrip() {
        RecipeIngredientDO dataObject = new RecipeIngredientDO();
        dataObject.setIngredientType("MEAT");
        dataObject.setMainIngredient(Boolean.TRUE);
        dataObject.setQuantity(new BigDecimal("2.50"));
        dataObject.setUnit("g");
        dataObject.setSortNo(3);

        RecipeIngredient entity = recipeIngredientInfraConvertor.toEntity(dataObject);

        assertEquals(IngredientType.MEAT, entity.getIngredientType());
        assertEquals(Boolean.TRUE, entity.getMainIngredient());
        assertEquals(new BigDecimal("2.50"), entity.getQuantity());
        assertEquals("g", entity.getUnit());
        assertEquals(3, entity.getSortNo());

        entity.setIngredientType(IngredientType.SEAFOOD);
        entity.setMainIngredient(Boolean.FALSE);
        entity.setQuantity(new BigDecimal("1.25"));

        RecipeIngredientDO roundTrip = recipeIngredientInfraConvertor.toDo(entity);

        assertEquals("SEAFOOD", roundTrip.getIngredientType());
        assertEquals(Boolean.FALSE, roundTrip.getMainIngredient());
        assertEquals(new BigDecimal("1.25"), roundTrip.getQuantity());
    }

    @Test
    void shouldConvertRecipeStepRoundTrip() {
        RecipeStepDO dataObject = new RecipeStepDO();
        dataObject.setStepNo(2);
        dataObject.setContent("Mix well");
        dataObject.setImageUrl("https://example.com/step.png");

        RecipeStep entity = recipeStepInfraConvertor.toEntity(dataObject);

        assertEquals(2, entity.getStepNo());
        assertEquals("Mix well", entity.getContent());
        assertEquals("https://example.com/step.png", entity.getImageUrl());

        entity.setStepNo(4);
        entity.setContent("Serve immediately");

        RecipeStepDO roundTrip = recipeStepInfraConvertor.toDo(entity);

        assertEquals(4, roundTrip.getStepNo());
        assertEquals("Serve immediately", roundTrip.getContent());
    }
}
