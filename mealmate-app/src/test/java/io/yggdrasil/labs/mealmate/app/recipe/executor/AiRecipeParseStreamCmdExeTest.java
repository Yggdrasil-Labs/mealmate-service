package io.yggdrasil.labs.mealmate.app.recipe.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
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
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatRequest;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatResult;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiErrorCode;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage.AiRole;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSession;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSessionRepository;
import io.yggdrasil.labs.mealmate.domain.common.ai.PromptSanitizer;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParseCache;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParsedData;
import io.yggdrasil.labs.mealmate.domain.recipe.model.enums.RecipeParseStatus;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeParseCacheRepository;

/**
 * AiRecipeParseStreamCmdExe 单测。
 *
 * <p>通过 Mockito doAnswer 模拟 streamChat 回调行为，验证：
 *
 * <ul>
 *   <li>AC1: onChunk 收到 3 次，onResult 收到完整 AiRecipeParseResultCO
 *   <li>AC2: LLM 返回 invalid JSON → onResult 保留已有 cache
 *   <li>AC5: 同一 sessionId 正在处理中 → BizException(AI_SESSION_BUSY)
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class AiRecipeParseStreamCmdExeTest {

    @Mock AiChatGateway chatGateway;
    @Mock AiSessionRepository sessionRepository;
    @Mock RecipeParseCacheRepository cacheRepository;
    @Mock PromptSanitizer sanitizer;
    @Mock RecipeParsePromptBuilder promptBuilder;

    @InjectMocks AiRecipeParseStreamCmdExe executor;

    private AiSession testSession;

    @BeforeEach
    void setUp() {
        AiRecipeParseStreamCmdExe.clearInFlightSessions();
        testSession =
                AiSession.builder()
                        .sessionId("stream-session-id")
                        .createdAt(LocalDateTime.now())
                        .build();
    }

    @AfterEach
    void tearDown() {
        AiRecipeParseStreamCmdExe.clearInFlightSessions();
    }

    /**
     * AC1: 正常流式响应 — onChunk 收到 3 次，onResult 收到完整的 AiRecipeParseResultCO（status + parsed +
     * sessionId）。
     */
    @Test
    void normalStream_onChunkCalledThreeTimes_onResultHasFullCO() {
        // Given: 已有 session，cache 中有部分数据
        AiRecipeParseChatCmd cmd = new AiRecipeParseChatCmd("stream-session-id", "补充步骤信息");
        mockExistingSession();
        mockExistingCache(
                RecipeParsedData.builder()
                        .name("番茄炒蛋")
                        .ingredients(
                                List.of(
                                        new RecipeParsedData.IngredientItem(
                                                "番茄", "VEGETABLE", 2.0, "个", true)))
                        .build());
        when(sanitizer.sanitize(anyString())).thenReturn("补充步骤信息");
        when(promptBuilder.buildMessages(any(), any(), anyString()))
                .thenReturn(
                        List.of(
                                new AiMessage(AiRole.SYSTEM, "sys"),
                                new AiMessage(AiRole.USER, "msg")));

        // 模拟 streamChat：发 3 次 onChunk，然后 onComplete
        String fullJson =
                "{\"steps\":[{\"stepNo\":1,\"content\":\"打散鸡蛋\"},{\"stepNo\":2,\"content\":\"翻炒\"}],\"reply\":\"已补充步骤\"}";
        mockStreamChat(List.of("{\"ste", "ps\":[{\"step", "No\":1}]}"), fullJson);

        // When
        List<String> chunks = new ArrayList<>();
        AtomicReference<AiRecipeParseResultCO> resultRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        executor.execute(cmd, chunks::add, resultRef::set, errorRef::set);

        // Then
        assertEquals(3, chunks.size(), "onChunk 应被调用 3 次");
        assertNotNull(resultRef.get(), "onResult 应被调用");

        AiRecipeParseResultCO result = resultRef.get();
        assertEquals("stream-session-id", result.getSessionId());
        assertNotNull(result.getParsed());
        assertEquals("番茄炒蛋", result.getParsed().getName());
        assertNotNull(result.getParsed().getSteps());
        assertEquals(RecipeParseStatus.READY_TO_CONFIRM, result.getStatus());
    }

    /** AC2: LLM 返回 invalid JSON → onResult 中 parsed 基于已有 cache，status 不变。 */
    @Test
    void invalidJson_preservesCache_onResultUsesAccumulated() {
        // Given: 已有 session，cache 中有部分数据
        AiRecipeParseChatCmd cmd = new AiRecipeParseChatCmd("stream-session-id", "乱输入");
        mockExistingSession();
        RecipeParsedData accumulated =
                RecipeParsedData.builder()
                        .name("红烧肉")
                        .ingredients(
                                List.of(
                                        new RecipeParsedData.IngredientItem(
                                                "五花肉", "MEAT", 500.0, "克", true)))
                        .build();
        mockExistingCache(accumulated);
        when(sanitizer.sanitize(anyString())).thenReturn("乱输入");
        when(promptBuilder.buildMessages(any(), any(), anyString()))
                .thenReturn(
                        List.of(
                                new AiMessage(AiRole.SYSTEM, "sys"),
                                new AiMessage(AiRole.USER, "msg")));

        // 模拟 streamChat：onComplete 返回无效 JSON
        mockStreamChat(List.of("chunk1"), "invalid json {{{not parseable");

        // When
        AtomicReference<AiRecipeParseResultCO> resultRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        executor.execute(cmd, chunk -> {}, resultRef::set, errorRef::set);

        // Then: onResult 被调用，parsed 为已有 cache 数据
        assertNotNull(resultRef.get());
        AiRecipeParseResultCO result = resultRef.get();
        assertEquals("红烧肉", result.getParsed().getName());
        // name 有但无 steps → REFINING
        assertEquals(RecipeParseStatus.REFINING, result.getStatus());
    }

    /** AC5: 同一 sessionId 正在处理中 → BizException(AI_SESSION_BUSY) 通过 onError 传递。 */
    @Test
    void concurrentSession_returnsBizExceptionAiSessionBusy() {
        // Given: 已有 session
        AiRecipeParseChatCmd cmd = new AiRecipeParseChatCmd("stream-session-id", "消息1");
        mockExistingSession();
        mockExistingCache(RecipeParsedData.builder().name("测试").build());
        when(sanitizer.sanitize(anyString())).thenReturn("消息1");
        when(promptBuilder.buildMessages(any(), any(), anyString()))
                .thenReturn(
                        List.of(
                                new AiMessage(AiRole.SYSTEM, "sys"),
                                new AiMessage(AiRole.USER, "msg")));

        // 模拟 streamChat：第一个请求挂起不完成（不调用 onComplete）
        doAnswer(
                        invocation -> {
                            // 不调用任何回调，模拟处理中状态
                            return null;
                        })
                .when(chatGateway)
                .streamChat(any(), any(), any(), any(), any());

        // When: 第一个请求开始
        AtomicReference<Exception> firstError = new AtomicReference<>();
        executor.execute(cmd, chunk -> {}, result -> {}, firstError::set);

        // When: 第二个请求尝试使用相同 sessionId
        AtomicReference<Exception> secondError = new AtomicReference<>();
        AiRecipeParseChatCmd cmd2 = new AiRecipeParseChatCmd("stream-session-id", "消息2");
        executor.execute(cmd2, chunk -> {}, result -> {}, secondError::set);

        // Then: 第二个请求收到 AI_SESSION_BUSY 错误
        assertNotNull(secondError.get());
        assertTrue(secondError.get() instanceof BizException);
        BizException bizEx = (BizException) secondError.get();
        assertEquals(AiErrorCode.AI_SESSION_BUSY.getCode(), bizEx.getErrCode());
    }

    // --- Helper methods ---

    private void mockExistingSession() {
        when(sessionRepository.findById("stream-session-id")).thenReturn(Optional.of(testSession));
    }

    private void mockExistingCache(RecipeParsedData accumulated) {
        when(cacheRepository.findBySessionId("stream-session-id"))
                .thenReturn(
                        Optional.of(
                                RecipeParseCache.builder()
                                        .accumulatedParsed(accumulated)
                                        .status(RecipeParseStatus.REFINING)
                                        .build()));
    }

    /**
     * 模拟 streamChat 行为：按顺序调用 onChunk，然后调用 onComplete。
     *
     * @param chunks 要发送的增量文本列表
     * @param fullContent onComplete 中完整的 content
     */
    @SuppressWarnings("unchecked")
    private void mockStreamChat(List<String> chunks, String fullContent) {
        doAnswer(
                        invocation -> {
                            Consumer<String> onChunk = invocation.getArgument(2);
                            Consumer<AiChatResult> onComplete = invocation.getArgument(3);

                            // 依次发送 chunks
                            for (String chunk : chunks) {
                                onChunk.accept(chunk);
                            }

                            // 流完成
                            onComplete.accept(AiChatResult.builder().content(fullContent).build());
                            return null;
                        })
                .when(chatGateway)
                .streamChat(
                        any(AiChatRequest.class),
                        any(AtomicBoolean.class),
                        any(Consumer.class),
                        any(Consumer.class),
                        any(Consumer.class));
    }
}
