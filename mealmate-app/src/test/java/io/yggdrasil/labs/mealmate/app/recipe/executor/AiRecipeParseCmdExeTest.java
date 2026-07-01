package io.yggdrasil.labs.mealmate.app.recipe.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeParseChatCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeParseResultCO;
import io.yggdrasil.labs.mealmate.app.recipe.prompt.RecipeParsePromptBuilder;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatGateway;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatResult;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage.AiRole;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSession;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSessionRepository;
import io.yggdrasil.labs.mealmate.domain.common.ai.PromptSanitizer;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParseCache;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParsedData;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeParseStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeParseCacheRepository;

@ExtendWith(MockitoExtension.class)
class AiRecipeParseCmdExeTest {

    @Mock AiChatGateway chatGateway;
    @Mock AiSessionRepository sessionRepository;
    @Mock RecipeParseCacheRepository cacheRepository;
    @Mock PromptSanitizer sanitizer;
    @Mock RecipeParsePromptBuilder promptBuilder;

    @InjectMocks AiRecipeParseCmdExe executor;

    private AiSession testSession;

    @BeforeEach
    void setUp() {
        testSession =
                AiSession.builder()
                        .sessionId("test-session-id")
                        .createdAt(LocalDateTime.now())
                        .build();
    }

    @Test
    void firstTurn_parsesRecipe_returnsRefiningStatus() {
        // Given: 新会话，LLM 返回有 name + ingredients 但无 steps
        AiRecipeParseChatCmd cmd = new AiRecipeParseChatCmd(null, "番茄炒蛋，2个番茄3个鸡蛋");
        mockNewSession();
        when(sanitizer.sanitize(anyString())).thenReturn("番茄炒蛋，2个番茄3个鸡蛋");
        when(promptBuilder.buildMessages(any(), any(), anyString()))
                .thenReturn(
                        List.of(
                                new AiMessage(AiRole.SYSTEM, "sys"),
                                new AiMessage(AiRole.USER, "msg")));
        when(chatGateway.chat(any())).thenReturn(validTomatoEggResult());

        // When
        AiRecipeParseResultCO result = executor.execute(cmd);

        // Then
        assertEquals(RecipeParseStatus.REFINING, result.getStatus());
        assertEquals("番茄炒蛋", result.getParsed().getName());
        assertNotNull(result.getSessionId());
        assertNotNull(result.getParsed().getIngredients());
        assertNull(result.getParsed().getSteps()); // steps null → REFINING
    }

    @Test
    void secondTurn_withSteps_returnsReadyToConfirm() {
        // Given: 已有会话 + 有累积数据（name + ingredients），本轮补 steps
        AiRecipeParseChatCmd cmd = new AiRecipeParseChatCmd("test-session-id", "先炒鸡蛋再加番茄");
        mockExistingSession();
        mockExistingCache(
                RecipeParsedData.builder()
                        .name("番茄炒蛋")
                        .ingredients(
                                List.of(
                                        new RecipeParsedData.IngredientItem(
                                                "番茄", "VEGETABLE", 2.0, "个", true)))
                        .build());
        when(sanitizer.sanitize(anyString())).thenReturn("先炒鸡蛋再加番茄");
        when(promptBuilder.buildMessages(any(), any(), anyString()))
                .thenReturn(
                        List.of(
                                new AiMessage(AiRole.SYSTEM, "sys"),
                                new AiMessage(AiRole.USER, "msg")));
        when(chatGateway.chat(any())).thenReturn(stepsOnlyResult());

        // When
        AiRecipeParseResultCO result = executor.execute(cmd);

        // Then: merge 后 steps 非 null → READY_TO_CONFIRM
        assertEquals(RecipeParseStatus.READY_TO_CONFIRM, result.getStatus());
        assertNotNull(result.getParsed().getSteps());
        assertEquals("番茄炒蛋", result.getParsed().getName()); // merge 保留旧值
        assertNotNull(result.getParsed().getIngredients()); // merge 保留旧值
    }

    @Test
    void llmInvalidJson_preservesAccumulatedParsed_returnsErrorReply() {
        // Given: LLM 返回无效 JSON
        AiRecipeParseChatCmd cmd = new AiRecipeParseChatCmd("test-session-id", "乱码输入");
        mockExistingSession();
        RecipeParsedData accumulated = RecipeParsedData.builder().name("番茄炒蛋").build();
        mockExistingCache(accumulated);
        when(sanitizer.sanitize(anyString())).thenReturn("乱码输入");
        when(promptBuilder.buildMessages(any(), any(), anyString()))
                .thenReturn(
                        List.of(
                                new AiMessage(AiRole.SYSTEM, "sys"),
                                new AiMessage(AiRole.USER, "msg")));
        when(chatGateway.chat(any()))
                .thenReturn(AiChatResult.builder().content("invalid json {{{").build());

        // When
        AiRecipeParseResultCO result = executor.execute(cmd);

        // Then: 保留 accumulated，返回错误 reply
        assertEquals("番茄炒蛋", result.getParsed().getName());
        assertEquals(
                RecipeParseStatus.REFINING, result.getStatus()); // name有但无ingredients → REFINING
    }

    @Test
    void maxTurns_returnsReadyToConfirm() {
        // Given: session 已有 10 轮
        AiRecipeParseChatCmd cmd = new AiRecipeParseChatCmd("test-session-id", "继续");
        AiSession fullSession =
                AiSession.builder()
                        .sessionId("test-session-id")
                        .createdAt(LocalDateTime.now())
                        .build();
        // 添加 10 轮对话
        for (int i = 0; i < 10; i++) {
            fullSession.addTurn(
                    new AiMessage(AiRole.USER, "turn " + i),
                    new AiMessage(AiRole.ASSISTANT, "reply " + i));
        }
        when(sessionRepository.findById("test-session-id")).thenReturn(Optional.of(fullSession));

        // When
        AiRecipeParseResultCO result = executor.execute(cmd);

        // Then: 超轮次 → READY_TO_CONFIRM
        assertEquals(RecipeParseStatus.READY_TO_CONFIRM, result.getStatus());
    }

    @Test
    void determineStatus_nullName_returnsParsing() {
        RecipeParsedData data = RecipeParsedData.builder().build();
        assertEquals(RecipeParseStatus.PARSING, executor.determineStatus(data));
    }

    @Test
    void determineStatus_emptyIngredients_returnsRefining() {
        RecipeParsedData data =
                RecipeParsedData.builder().name("test").ingredients(List.of()).build();
        assertEquals(RecipeParseStatus.REFINING, executor.determineStatus(data));
    }

    @Test
    void determineStatus_nullSteps_returnsRefining() {
        RecipeParsedData data =
                RecipeParsedData.builder()
                        .name("test")
                        .ingredients(
                                List.of(
                                        new RecipeParsedData.IngredientItem(
                                                "a", "MEAT", 1.0, "个", true)))
                        .steps(null)
                        .build();
        assertEquals(RecipeParseStatus.REFINING, executor.determineStatus(data));
    }

    @Test
    void determineStatus_emptySteps_returnsReadyToConfirm() {
        // 空列表 = 明确不需要步骤 → 可确认
        RecipeParsedData data =
                RecipeParsedData.builder()
                        .name("test")
                        .ingredients(
                                List.of(
                                        new RecipeParsedData.IngredientItem(
                                                "a", "MEAT", 1.0, "个", true)))
                        .steps(List.of())
                        .build();
        assertEquals(RecipeParseStatus.READY_TO_CONFIRM, executor.determineStatus(data));
    }

    @Test
    void merge_nullFieldsDoNotOverwrite() {
        RecipeParsedData old =
                RecipeParsedData.builder().name("旧菜名").recipeType("HOME_COOKING").build();
        RecipeParsedData newData =
                RecipeParsedData.builder().name(null).recipeType("BAKING").build();
        RecipeParsedData merged = executor.mergeParsed(old, newData);
        assertEquals("旧菜名", merged.getName()); // null 不覆盖
        assertEquals("BAKING", merged.getRecipeType()); // 非 null 覆盖
    }

    @Test
    void merge_nonNullFieldsOverwrite() {
        RecipeParsedData old = RecipeParsedData.builder().name("旧菜名").build();
        RecipeParsedData newData = RecipeParsedData.builder().name("新菜名").build();
        RecipeParsedData merged = executor.mergeParsed(old, newData);
        assertEquals("新菜名", merged.getName());
    }

    // --- Helper methods ---

    private void mockNewSession() {
        when(sessionRepository.create(any())).thenReturn("test-session-id");
        when(sessionRepository.findById("test-session-id")).thenReturn(Optional.of(testSession));
        when(cacheRepository.findBySessionId("test-session-id")).thenReturn(Optional.empty());
    }

    private void mockExistingSession() {
        when(sessionRepository.findById("test-session-id")).thenReturn(Optional.of(testSession));
    }

    private void mockExistingCache(RecipeParsedData accumulated) {
        when(cacheRepository.findBySessionId("test-session-id"))
                .thenReturn(
                        Optional.of(
                                RecipeParseCache.builder()
                                        .accumulatedParsed(accumulated)
                                        .status(RecipeParseStatus.REFINING)
                                        .build()));
    }

    private AiChatResult validTomatoEggResult() {
        String json =
                """
{"name":"番茄炒蛋","recipeType":"HOME_COOKING","cookingTimeMin":10,\
"ingredients":[{"ingredientName":"番茄","ingredientType":"VEGETABLE","quantity":2.0,"unit":"个","mainIngredient":true}],\
"steps":null,"reply":"已解析番茄炒蛋基本信息"}""";
        return AiChatResult.builder().content(json).build();
    }

    private AiChatResult stepsOnlyResult() {
        String json =
                """
                {"steps":[{"stepNo":1,"content":"打散鸡蛋"},{"stepNo":2,"content":"加入番茄翻炒"}],\
                "reply":"已补充烹饪步骤"}""";
        return AiChatResult.builder().content(json).build();
    }
}
