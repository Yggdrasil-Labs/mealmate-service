package io.yggdrasil.labs.mealmate.start;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import jakarta.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.CrowdTag;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.DifficultyLevel;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.IngredientType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.SeasonTag;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.impl.RecipeRepositoryImpl;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor.RecipeInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor.RecipeIngredientInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor.RecipeNutritionInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.convertor.RecipeStepInfraConvertor;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.service.RecipeIngredientService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.service.RecipeNutritionService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.service.RecipeService;
import io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe.dataobject.service.RecipeStepService;

@SpringBootTest(
        classes = CreateRecipeApiIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CreateRecipeApiIntegrationTest {

    @Resource private MockMvc mockMvc;
    @Resource private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM recipe_ingredient");
        jdbcTemplate.update("DELETE FROM recipe_step");
        jdbcTemplate.update("DELETE FROM recipe_nutrition");
        jdbcTemplate.update("DELETE FROM recipe");
    }

    @Test
    void shouldCreateRecipeViaHttpApiAndPersistChildRows() throws Exception {
        Integer recipeBefore = countRows("recipe");
        Integer ingredientBefore = countRows("recipe_ingredient");
        Integer stepBefore = countRows("recipe_step");
        Integer nutritionBefore = countRows("recipe_nutrition");

        MvcResult result =
                mockMvc.perform(
                                post("/api/recipes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        buildCreateRecipePayload())))
                        .andExpect(status().isOk())
                        .andReturn();

        assertEquals(recipeBefore + 1, countRows("recipe"));
        assertEquals(ingredientBefore + 2, countRows("recipe_ingredient"));
        assertEquals(stepBefore + 2, countRows("recipe_step"));
        assertEquals(nutritionBefore + 1, countRows("recipe_nutrition"));
        assertTrue(
                objectMapper
                                .readTree(result.getResponse().getContentAsString())
                                .path("data")
                                .path("ingredients")
                                .get(0)
                                .path("id")
                                .asLong()
                        > 0);
        assertTrue(
                objectMapper
                                .readTree(result.getResponse().getContentAsString())
                                .path("data")
                                .path("steps")
                                .get(0)
                                .path("id")
                                .asLong()
                        > 0);
        assertTrue(
                objectMapper
                                .readTree(result.getResponse().getContentAsString())
                                .path("data")
                                .path("nutritionFact")
                                .path("id")
                                .asLong()
                        > 0);
    }

    @Test
    void shouldPageRecipesWithFilterConditionViaHttpApi() throws Exception {
        mockMvc.perform(
                        post("/api/recipes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                buildCreateRecipePayload())))
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/api/recipes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                new CreateRecipePayload(
                                                        "Recipe API Autumn Dessert",
                                                        RecipeType.DESSERT,
                                                        SeasonTag.AUTUMN,
                                                        CrowdTag.GENERAL,
                                                        List.of("sweet"),
                                                        DifficultyLevel.EASY,
                                                        18,
                                                        "https://example.com/dessert.jpg",
                                                        false,
                                                        false,
                                                        List.of(
                                                                new RecipeIngredientPayload(
                                                                        "Chocolate",
                                                                        IngredientType.OTHER,
                                                                        new BigDecimal("50"),
                                                                        "g",
                                                                        true,
                                                                        1)),
                                                        List.of(
                                                                new RecipeStepPayload(
                                                                        1, "Mix well", null)),
                                                        new NutritionFactPayload(
                                                                new BigDecimal("220"),
                                                                new BigDecimal("3.5"),
                                                                new BigDecimal("9.0"),
                                                                new BigDecimal("28.0"),
                                                                new BigDecimal("2.0"),
                                                                new BigDecimal("40.0"),
                                                                new BigDecimal("120.0"),
                                                                java.util.Map.of(
                                                                        "source", "manual"))))))
                .andExpect(status().isOk());

        MvcResult result =
                mockMvc.perform(
                                get("/api/recipes")
                                        .param("recipeType", RecipeType.SOUP.name())
                                        .param("pageNum", "1")
                                        .param("pageSize", "10"))
                        .andExpect(status().isOk())
                        .andReturn();

        String body = result.getResponse().getContentAsString();
        assertTrue(body.contains("Recipe API Winter Soup"));
        assertFalse(body.contains("Recipe API Autumn Dessert"));
        assertEquals(1, objectMapper.readTree(body).path("totalCount").asInt());
    }

    @Test
    void shouldAllowReusingDeletedRecipeNameMultipleTimes() throws Exception {
        Long firstRecipeId = createRecipeAndReturnId("Reusable Recipe");
        mockMvc.perform(delete("/api/recipes/{recipeId}", firstRecipeId))
                .andExpect(status().isOk());

        Long secondRecipeId = createRecipeAndReturnId("Reusable Recipe");
        mockMvc.perform(delete("/api/recipes/{recipeId}", secondRecipeId))
                .andExpect(status().isOk());

        assertEquals(2, countRowsByName("Reusable Recipe"));
        assertEquals(0, countActiveRowsByName("Reusable Recipe"));
    }

    private Integer countRows(String tableName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }

    private Integer countRowsByName(String recipeName) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM recipe WHERE name = ?", Integer.class, recipeName);
    }

    private Integer countActiveRowsByName(String recipeName) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM recipe WHERE name = ? AND deleted = 0",
                Integer.class,
                recipeName);
    }

    private Long createRecipeAndReturnId(String recipeName) throws Exception {
        CreateRecipePayload payload = buildCreateRecipePayload(recipeName);
        MvcResult result =
                mockMvc.perform(
                                post("/api/recipes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(payload)))
                        .andExpect(status().isOk())
                        .andReturn();
        return objectMapper
                .readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asLong();
    }

    private CreateRecipePayload buildCreateRecipePayload() {
        return buildCreateRecipePayload("Recipe API Winter Soup");
    }

    private CreateRecipePayload buildCreateRecipePayload(String recipeName) {
        return new CreateRecipePayload(
                recipeName,
                RecipeType.SOUP,
                SeasonTag.WINTER,
                CrowdTag.BABY,
                List.of("warm", "healthy"),
                DifficultyLevel.MEDIUM,
                25,
                "https://example.com/winter-soup.jpg",
                true,
                false,
                List.of(
                        new RecipeIngredientPayload(
                                "Pumpkin",
                                IngredientType.VEGETABLE,
                                new BigDecimal("300"),
                                "g",
                                true,
                                1),
                        new RecipeIngredientPayload(
                                "Chicken",
                                IngredientType.MEAT,
                                new BigDecimal("120"),
                                "g",
                                false,
                                2)),
                List.of(
                        new RecipeStepPayload(1, "Prepare ingredients", null),
                        new RecipeStepPayload(2, "Simmer until tender", null)),
                new NutritionFactPayload(
                        new BigDecimal("180"),
                        new BigDecimal("12.5"),
                        new BigDecimal("4.2"),
                        new BigDecimal("15.0"),
                        new BigDecimal("3.0"),
                        new BigDecimal("30.0"),
                        new BigDecimal("560.0"),
                        java.util.Map.of("notes", "balanced")));
    }

    private record CreateRecipePayload(
            String name,
            RecipeType recipeType,
            SeasonTag seasonTag,
            CrowdTag crowdTag,
            List<String> tasteTags,
            DifficultyLevel difficultyLevel,
            Integer cookingTimeMin,
            String coverImageUrl,
            Boolean babyFriendly,
            Boolean weightLossFriendly,
            List<RecipeIngredientPayload> ingredients,
            List<RecipeStepPayload> steps,
            NutritionFactPayload nutritionFact) {}

    private record RecipeIngredientPayload(
            String ingredientName,
            IngredientType ingredientType,
            BigDecimal quantity,
            String unit,
            Boolean mainIngredient,
            Integer sortNo) {}

    private record RecipeStepPayload(Integer stepNo, String content, String imageUrl) {}

    private record NutritionFactPayload(
            BigDecimal calories,
            BigDecimal protein,
            BigDecimal fat,
            BigDecimal carbohydrate,
            BigDecimal fiber,
            BigDecimal calcium,
            BigDecimal sodium,
            java.util.Map<String, Object> nutritionJson) {}

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ComponentScan(
            basePackages = {
                "io.yggdrasil.labs.mealmate.adapter.web.recipe",
                "io.yggdrasil.labs.mealmate.app.recipe",
                "io.yggdrasil.labs.mealmate.infrastructure.persistence.recipe"
            })
    @MapperScan(
            basePackages = "io.yggdrasil.labs.mealmate.infrastructure.persistence",
            markerInterface = BaseMapper.class)
    static class TestApplication {

        @Bean
        RecipeRepositoryImpl recipeRepositoryImpl(
                RecipeInfraConvertor recipeInfraConvertor,
                RecipeIngredientInfraConvertor recipeIngredientInfraConvertor,
                RecipeStepInfraConvertor recipeStepInfraConvertor,
                RecipeNutritionInfraConvertor recipeNutritionInfraConvertor,
                RecipeService recipeService,
                RecipeIngredientService recipeIngredientService,
                RecipeStepService recipeStepService,
                RecipeNutritionService recipeNutritionService) {
            return new RecipeRepositoryImpl(
                    recipeInfraConvertor,
                    recipeIngredientInfraConvertor,
                    recipeStepInfraConvertor,
                    recipeNutritionInfraConvertor,
                    recipeService,
                    recipeIngredientService,
                    recipeStepService,
                    recipeNutritionService);
        }
    }
}
