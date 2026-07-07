package io.yggdrasil.labs.mealmate.app.mealplan.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.app.mealplan.context.MealPlanContext;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;

class AiMealPlanResultParserTest {

    private AiMealPlanResultParser parser;
    private MealPlanContext context;
    private List<Recipe> testRecipes;
    private static final LocalDate WEEK_START = LocalDate.of(2026, 7, 6);
    private static final Long FAMILY_ID = 1L;

    @BeforeEach
    void setUp() {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        parser = new AiMealPlanResultParser(om);

        // 准备 5 个测试 Recipe
        // id=1: 10min, babyFriendly=true, weightLoss=false
        // id=2: 15min, babyFriendly=false, weightLoss=true
        // id=3: 30min, babyFriendly=false, weightLoss=false
        // id=4: 45min, babyFriendly=true, weightLoss=false
        // id=5: 60min, babyFriendly=false, weightLoss=true
        testRecipes =
                Arrays.asList(
                        buildRecipe(1L, 10, true, false),
                        buildRecipe(2L, 15, false, true),
                        buildRecipe(3L, 30, false, false),
                        buildRecipe(4L, 45, true, false),
                        buildRecipe(5L, 60, false, true));

        context =
                MealPlanContext.builder()
                        .candidateIds(Arrays.asList(1L, 2L, 3L, 4L, 5L))
                        .candidateRecipes(testRecipes)
                        .avoidIngredients(new HashSet<>())
                        .allergyIngredients(new HashSet<>())
                        .build();
    }

    /** 合法 7天 JSON → 35 items。 */
    @Test
    void parse_validJson_returns35Items() {
        String json = buildValid7DayJson();

        ParsedMealPlanResult result = parser.parse(json, context, FAMILY_ID, WEEK_START);

        assertEquals(35, result.getItems().size());
        // 验证每天 5 个条目（1 早 + 2 午 + 2 晚）
        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate date = WEEK_START.plusDays(dayOffset);
            long count =
                    result.getItems().stream()
                            .filter(item -> item.getMealDate().equals(date))
                            .count();
            assertEquals(5, count, "Day " + date + " should have 5 items");
        }
    }

    /** 某 recipeId 不在 candidateIds → 被替换为有效 ID。 */
    @Test
    void parse_invalidRecipeId_replaced() {
        // 使用 recipeId=999（不在候选池中）
        String json = buildJsonWithInvalidId();

        ParsedMealPlanResult result = parser.parse(json, context, FAMILY_ID, WEEK_START);

        // 所有 item 的 recipeId 都应在有效范围内
        Set<Long> validIds = new HashSet<>(context.getCandidateIds());
        for (MealPlanItem item : result.getItems()) {
            assertTrue(
                    validIds.contains(item.getRecipeId()),
                    "recipeId " + item.getRecipeId() + " should be in valid set");
        }
    }

    /** 早餐替换优先选 ≤ 20min。 */
    @Test
    void parse_breakfastReplacement_prefersShortCookTime() {
        // 第一天早餐 recipeId=999（无效），触发替换
        String json = buildJsonWithInvalidBreakfast();

        ParsedMealPlanResult result = parser.parse(json, context, FAMILY_ID, WEEK_START);

        // 第一天早餐应被替换为 cookingTimeMin ≤ 20 的菜品（id=1 或 id=2）
        MealPlanItem firstBreakfast =
                result.getItems().stream()
                        .filter(
                                item ->
                                        item.getMealDate().equals(WEEK_START)
                                                && item.getMealType() == MealType.BREAKFAST)
                        .findFirst()
                        .orElse(null);
        assertNotNull(firstBreakfast);
        // id=1 (10min) 或 id=2 (15min)
        assertTrue(
                firstBreakfast.getRecipeId() == 1L || firstBreakfast.getRecipeId() == 2L,
                "Breakfast replacement should prefer quick cook recipe, got id="
                        + firstBreakfast.getRecipeId());
    }

    /** 只有 5 天数据 → items 仍 = 35。 */
    @Test
    void parse_missingDay_fillsTo35() {
        String json = buildJsonWith5Days();

        ParsedMealPlanResult result = parser.parse(json, context, FAMILY_ID, WEEK_START);

        assertEquals(35, result.getItems().size());
    }

    /** 午餐给了 4 道 → 截取前 2 道。 */
    @Test
    void parse_excessMealItems_truncated() {
        String json = buildJsonWithExcessLunch();

        ParsedMealPlanResult result = parser.parse(json, context, FAMILY_ID, WEEK_START);

        // 第一天午餐应只有 2 个
        long lunchCount =
                result.getItems().stream()
                        .filter(
                                item ->
                                        item.getMealDate().equals(WEEK_START)
                                                && item.getMealType() == MealType.LUNCH)
                        .count();
        assertEquals(2, lunchCount);
    }

    /** 非法 JSON → AiMealPlanParseException。 */
    @Test
    void parse_invalidJson_throwsException() {
        String invalidJson = "this is not valid json {{{";

        assertThrows(
                AiMealPlanParseException.class,
                () -> parser.parse(invalidJson, context, FAMILY_ID, WEEK_START));
    }

    /** reasoning map 包含 7 天 key。 */
    @Test
    void parse_reasoningExtracted() {
        String json = buildValid7DayJson();

        ParsedMealPlanResult result = parser.parse(json, context, FAMILY_ID, WEEK_START);

        Map<String, String> reasoning = result.getReasoning();
        assertEquals(7, reasoning.size());
        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            String dateStr = WEEK_START.plusDays(dayOffset).toString();
            assertTrue(reasoning.containsKey(dateStr), "reasoning should contain key " + dateStr);
        }
    }

    // ─── 辅助方法 ───

    private Recipe buildRecipe(
            Long id, int cookingTimeMin, boolean babyFriendly, boolean weightLoss) {
        return Recipe.builder()
                .id(id)
                .name("Recipe-" + id)
                .cookingTimeMin(cookingTimeMin)
                .babyFriendly(babyFriendly)
                .weightLossFriendly(weightLoss)
                .build();
    }

    /** 构造合法 7 天 JSON。每天 breakfast=1, lunch=2, dinner=2，使用有效 recipeId。 */
    private String buildValid7DayJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"days\":[");
        for (int i = 0; i < 7; i++) {
            if (i > 0) sb.append(",");
            String date = WEEK_START.plusDays(i).toString();
            sb.append("{\"date\":\"").append(date).append("\",");
            sb.append("\"breakfast\":[{\"recipeId\":1,\"recipeName\":\"R1\"}],");
            sb.append(
                    "\"lunch\":[{\"recipeId\":2,\"recipeName\":\"R2\"},{\"recipeId\":3,\"recipeName\":\"R3\"}],");
            sb.append(
                    "\"dinner\":[{\"recipeId\":4,\"recipeName\":\"R4\"},{\"recipeId\":5,\"recipeName\":\"R5\"}],");
            sb.append("\"reasoning\":\"Day ").append(i + 1).append(" reasoning\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /** 构造含无效 recipeId 的 JSON（第一天早餐 id=999）。 */
    private String buildJsonWithInvalidId() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"days\":[");
        for (int i = 0; i < 7; i++) {
            if (i > 0) sb.append(",");
            String date = WEEK_START.plusDays(i).toString();
            sb.append("{\"date\":\"").append(date).append("\",");
            if (i == 0) {
                // 第一天午餐含无效 id
                sb.append("\"breakfast\":[{\"recipeId\":1,\"recipeName\":\"R1\"}],");
                sb.append(
                        "\"lunch\":[{\"recipeId\":999,\"recipeName\":\"Invalid\"},{\"recipeId\":3,\"recipeName\":\"R3\"}],");
            } else {
                sb.append("\"breakfast\":[{\"recipeId\":1,\"recipeName\":\"R1\"}],");
                sb.append(
                        "\"lunch\":[{\"recipeId\":2,\"recipeName\":\"R2\"},{\"recipeId\":3,\"recipeName\":\"R3\"}],");
            }
            sb.append(
                    "\"dinner\":[{\"recipeId\":4,\"recipeName\":\"R4\"},{\"recipeId\":5,\"recipeName\":\"R5\"}],");
            sb.append("\"reasoning\":\"reason\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /** 构造第一天早餐含无效 id 的 JSON。 */
    private String buildJsonWithInvalidBreakfast() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"days\":[");
        for (int i = 0; i < 7; i++) {
            if (i > 0) sb.append(",");
            String date = WEEK_START.plusDays(i).toString();
            sb.append("{\"date\":\"").append(date).append("\",");
            if (i == 0) {
                sb.append("\"breakfast\":[{\"recipeId\":999,\"recipeName\":\"Invalid\"}],");
            } else {
                sb.append("\"breakfast\":[{\"recipeId\":1,\"recipeName\":\"R1\"}],");
            }
            sb.append(
                    "\"lunch\":[{\"recipeId\":2,\"recipeName\":\"R2\"},{\"recipeId\":3,\"recipeName\":\"R3\"}],");
            sb.append(
                    "\"dinner\":[{\"recipeId\":4,\"recipeName\":\"R4\"},{\"recipeId\":5,\"recipeName\":\"R5\"}],");
            sb.append("\"reasoning\":\"reason\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /** 构造只有 5 天的 JSON（缺少最后 2 天）。 */
    private String buildJsonWith5Days() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"days\":[");
        for (int i = 0; i < 5; i++) {
            if (i > 0) sb.append(",");
            String date = WEEK_START.plusDays(i).toString();
            sb.append("{\"date\":\"").append(date).append("\",");
            sb.append("\"breakfast\":[{\"recipeId\":1,\"recipeName\":\"R1\"}],");
            sb.append(
                    "\"lunch\":[{\"recipeId\":2,\"recipeName\":\"R2\"},{\"recipeId\":3,\"recipeName\":\"R3\"}],");
            sb.append(
                    "\"dinner\":[{\"recipeId\":4,\"recipeName\":\"R4\"},{\"recipeId\":5,\"recipeName\":\"R5\"}],");
            sb.append("\"reasoning\":\"reason\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    /** 构造第一天午餐有 4 道的 JSON。 */
    private String buildJsonWithExcessLunch() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"days\":[");
        for (int i = 0; i < 7; i++) {
            if (i > 0) sb.append(",");
            String date = WEEK_START.plusDays(i).toString();
            sb.append("{\"date\":\"").append(date).append("\",");
            sb.append("\"breakfast\":[{\"recipeId\":1,\"recipeName\":\"R1\"}],");
            if (i == 0) {
                // 4 道午餐
                sb.append(
                        "\"lunch\":[{\"recipeId\":1,\"recipeName\":\"R1\"},{\"recipeId\":2,\"recipeName\":\"R2\"},{\"recipeId\":3,\"recipeName\":\"R3\"},{\"recipeId\":4,\"recipeName\":\"R4\"}],");
            } else {
                sb.append(
                        "\"lunch\":[{\"recipeId\":2,\"recipeName\":\"R2\"},{\"recipeId\":3,\"recipeName\":\"R3\"}],");
            }
            sb.append(
                    "\"dinner\":[{\"recipeId\":4,\"recipeName\":\"R4\"},{\"recipeId\":5,\"recipeName\":\"R5\"}],");
            sb.append("\"reasoning\":\"reason\"}");
        }
        sb.append("]}");
        return sb.toString();
    }
}
