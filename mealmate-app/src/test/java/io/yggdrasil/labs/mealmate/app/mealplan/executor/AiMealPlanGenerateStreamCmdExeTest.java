package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import io.yggdrasil.labs.mealmate.app.mealplan.context.MealPlanContext;
import io.yggdrasil.labs.mealmate.app.mealplan.context.MealPlanContextBuilder;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AiMealPlanGenerateCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.AiMealPlanResultCO;
import io.yggdrasil.labs.mealmate.app.mealplan.parser.AiMealPlanResultParser;
import io.yggdrasil.labs.mealmate.app.mealplan.parser.ParsedMealPlanResult;
import io.yggdrasil.labs.mealmate.app.mealplan.prompt.MealPlanPromptBuilder;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatGateway;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatRequest;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatResult;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiErrorCode;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage;
import io.yggdrasil.labs.mealmate.domain.common.ai.PromptSanitizer;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.MealPlanItem;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealPlanCrowdType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.MealType;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanSource;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.DuplicateCheckDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.WeekPlanGenerateDomainService;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;

/**
 * AiMealPlanGenerateStreamCmdExe 单测。
 *
 * <p>通过 Mockito doAnswer 模拟 streamChat 回调行为，验证：
 *
 * <ul>
 *   <li>AC3: 正常流程 → onResult(fallback=false, reasoning 非空)
 *   <li>AC4: streamChat onError → fallback 规则引擎 → onResult(fallback=true)
 *   <li>AC5: 并发互斥 → BizException(AI_SESSION_BUSY)
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class AiMealPlanGenerateStreamCmdExeTest {

    @Mock MealPlanContextBuilder contextBuilder;
    @Mock MealPlanPromptBuilder promptBuilder;
    @Mock AiMealPlanResultParser resultParser;
    @Mock AiChatGateway chatGateway;
    @Mock PromptSanitizer sanitizer;
    @Mock WeeklyMealPlanRepository weeklyMealPlanRepository;
    @Mock DuplicateCheckDomainService duplicateCheckDomainService;
    @Mock WeekPlanGenerateDomainService weekPlanGenerateDomainService;

    @InjectMocks AiMealPlanGenerateStreamCmdExe executor;

    /** 测试用周一日期 */
    private static final LocalDate MONDAY = LocalDate.of(2026, 7, 6);

    /** 测试用家庭 ID */
    private static final Long FAMILY_ID = 1L;

    @BeforeEach
    void setUp() {
        AiMealPlanGenerateStreamCmdExe.clearInFlightRequests();
    }

    @AfterEach
    void tearDown() {
        AiMealPlanGenerateStreamCmdExe.clearInFlightRequests();
    }

    /** AC3: 正常流式响应 → onResult(fallback=false, reasoning 非空)。 */
    @Test
    void normalStream_onResultHasFallbackFalseAndReasoning() {
        // Given
        AiMealPlanGenerateCmd cmd =
                AiMealPlanGenerateCmd.builder()
                        .familyId(FAMILY_ID)
                        .weekStartDate(MONDAY)
                        .userHint("清淡一些")
                        .build();

        MealPlanContext context = buildTestContext();
        when(contextBuilder.build(FAMILY_ID)).thenReturn(context);
        when(sanitizer.sanitize(anyString())).thenReturn("清淡一些");
        when(promptBuilder.buildMessages(any(), any(), any(), any(), anyString()))
                .thenReturn(List.of(new AiMessage(AiMessage.AiRole.SYSTEM, "prompt")));

        // 模拟解析结果
        ParsedMealPlanResult parsedResult =
                ParsedMealPlanResult.builder()
                        .items(buildTestItems())
                        .reasoning(Map.of("2026-07-06", "家庭口味偏清淡"))
                        .build();
        when(resultParser.parse(anyString(), any(), anyLong(), any())).thenReturn(parsedResult);

        // 模拟保存
        WeeklyMealPlan savedPlan = buildSavedPlan();
        when(weeklyMealPlanRepository.save(any())).thenReturn(savedPlan);

        // 模拟 streamChat：发 3 次 chunk，然后 onComplete
        String fullJson = "{\"days\":{\"2026-07-06\":{\"reasoning\":\"家庭口味偏清淡\"}}}";
        mockStreamChatSuccess(List.of("chunk1", "chunk2", "chunk3"), fullJson);

        // When
        List<String> chunks = new ArrayList<>();
        AtomicReference<AiMealPlanResultCO> resultRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        executor.execute(cmd, chunks::add, resultRef::set, errorRef::set);

        // Then
        assertEquals(3, chunks.size(), "onChunk 应被调用 3 次");
        assertNotNull(resultRef.get(), "onResult 应被调用");

        AiMealPlanResultCO result = resultRef.get();
        assertFalse(result.isFallback(), "正常流程 fallback 应为 false");
        assertNotNull(result.getReasoning(), "reasoning 应非空");
        assertFalse(result.getReasoning().isEmpty(), "reasoning 不应为空 map");
    }

    /** AC4: streamChat onError → fallback 规则引擎 → onResult(fallback=true)。 */
    @Test
    void streamError_fallbackToRuleEngine_onResultHasFallbackTrue() {
        // Given
        AiMealPlanGenerateCmd cmd =
                AiMealPlanGenerateCmd.builder().familyId(FAMILY_ID).weekStartDate(MONDAY).build();

        MealPlanContext context = buildTestContext();
        when(contextBuilder.build(FAMILY_ID)).thenReturn(context);
        when(sanitizer.sanitize(anyString())).thenReturn("");
        when(promptBuilder.buildMessages(any(), any(), any(), any(), anyString()))
                .thenReturn(List.of(new AiMessage(AiMessage.AiRole.SYSTEM, "prompt")));

        // 模拟规则引擎 fallback 结果
        WeeklyMealPlan fallbackPlan = buildSavedPlan();
        fallbackPlan.setPlanSource(PlanSource.RULE_ENGINE);
        when(weekPlanGenerateDomainService.generate(anyLong(), any(), anyList()))
                .thenReturn(fallbackPlan);
        when(weeklyMealPlanRepository.save(any())).thenReturn(fallbackPlan);

        // 模拟 streamChat：触发 onError
        mockStreamChatError(new RuntimeException("AI service timeout"));

        // When
        AtomicReference<AiMealPlanResultCO> resultRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();

        executor.execute(cmd, chunk -> {}, resultRef::set, errorRef::set);

        // Then: fallback 应触发，onResult 被调用
        assertNotNull(resultRef.get(), "onResult 应被调用（通过 fallback）");
        AiMealPlanResultCO result = resultRef.get();
        assertTrue(result.isFallback(), "fallback 应为 true");
    }

    /** AC5: 同一 familyId+weekStartDate 正在处理中 → BizException(AI_SESSION_BUSY)。 */
    @Test
    void concurrentRequest_returnsBizExceptionAiSessionBusy() {
        // Given
        AiMealPlanGenerateCmd cmd =
                AiMealPlanGenerateCmd.builder().familyId(FAMILY_ID).weekStartDate(MONDAY).build();

        MealPlanContext context = buildTestContext();
        when(contextBuilder.build(FAMILY_ID)).thenReturn(context);
        when(sanitizer.sanitize(anyString())).thenReturn("");
        when(promptBuilder.buildMessages(any(), any(), any(), any(), anyString()))
                .thenReturn(List.of(new AiMessage(AiMessage.AiRole.SYSTEM, "prompt")));

        // 模拟 streamChat：第一个请求挂起不完成
        doAnswer(
                        invocation -> {
                            // 不调用任何回调，模拟处理中状态
                            return null;
                        })
                .when(chatGateway)
                .streamChat(any(), any(), any(), any(), any());

        // When: 第一个请求开始
        executor.execute(cmd, chunk -> {}, result -> {}, error -> {});

        // When: 第二个请求尝试使用相同 familyId + weekStartDate
        AtomicReference<Exception> secondError = new AtomicReference<>();
        AiMealPlanGenerateCmd cmd2 =
                AiMealPlanGenerateCmd.builder().familyId(FAMILY_ID).weekStartDate(MONDAY).build();
        executor.execute(cmd2, chunk -> {}, result -> {}, secondError::set);

        // Then: 第二个请求收到 AI_SESSION_BUSY 错误
        assertNotNull(secondError.get());
        assertTrue(secondError.get() instanceof BizException);
        BizException bizEx = (BizException) secondError.get();
        assertEquals(AiErrorCode.AI_SESSION_BUSY.getCode(), bizEx.getErrCode());
    }

    // --- Helper methods ---

    /** 构建测试用 MealPlanContext。 */
    private MealPlanContext buildTestContext() {
        Recipe testRecipe =
                Recipe.builder()
                        .id(1L)
                        .name("番茄炒蛋")
                        .babyFriendly(true)
                        .weightLossFriendly(true)
                        .cookingTimeMin(15)
                        .build();

        return MealPlanContext.builder()
                .familySummary("爸爸(30)+妈妈(28)+宝宝(2)")
                .preferenceSummary("忌辣，无过敏")
                .recipeCatalog("1-番茄炒蛋")
                .candidateIds(List.of(1L))
                .candidateRecipes(List.of(testRecipe))
                .build();
    }

    /** 构建测试用 MealPlanItem 列表。 */
    private List<MealPlanItem> buildTestItems() {
        return List.of(
                MealPlanItem.builder()
                        .mealDate(MONDAY)
                        .mealType(MealType.BREAKFAST)
                        .recipeId(1L)
                        .crowdType(MealPlanCrowdType.FAMILY)
                        .sortOrder(1)
                        .build());
    }

    /** 构建测试用已保存的 WeeklyMealPlan。 */
    private WeeklyMealPlan buildSavedPlan() {
        return WeeklyMealPlan.builder()
                .id(100L)
                .familyId(FAMILY_ID)
                .weekStartDate(MONDAY)
                .weekEndDate(MONDAY.plusDays(6))
                .status(PlanStatus.DRAFT)
                .planSource(PlanSource.AI_GENERATED)
                .generatedTime(LocalDateTime.now())
                .items(buildTestItems())
                .build();
    }

    /**
     * 模拟 streamChat 正常完成：按顺序调用 onChunk，然后调用 onComplete。
     *
     * @param chunks 要发送的增量文本列表
     * @param fullContent onComplete 中完整的 content
     */
    @SuppressWarnings("unchecked")
    private void mockStreamChatSuccess(List<String> chunks, String fullContent) {
        doAnswer(
                        invocation -> {
                            Consumer<String> onChunk = invocation.getArgument(2);
                            Consumer<AiChatResult> onComplete = invocation.getArgument(3);

                            for (String chunk : chunks) {
                                onChunk.accept(chunk);
                            }

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

    /**
     * 模拟 streamChat 出错：调用 onError 回调。
     *
     * @param error 要传递给 onError 的异常
     */
    @SuppressWarnings("unchecked")
    private void mockStreamChatError(Exception error) {
        doAnswer(
                        invocation -> {
                            Consumer<Exception> onError = invocation.getArgument(4);
                            onError.accept(error);
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
