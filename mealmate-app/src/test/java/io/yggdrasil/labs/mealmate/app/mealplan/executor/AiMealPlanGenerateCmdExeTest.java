package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.yggdrasil.labs.mealmate.app.mealplan.context.MealPlanContext;
import io.yggdrasil.labs.mealmate.app.mealplan.context.MealPlanContextBuilder;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AiMealPlanGenerateCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.AiMealPlanResultCO;
import io.yggdrasil.labs.mealmate.app.mealplan.parser.AiMealPlanParseException;
import io.yggdrasil.labs.mealmate.app.mealplan.parser.AiMealPlanResultParser;
import io.yggdrasil.labs.mealmate.app.mealplan.parser.ParsedMealPlanResult;
import io.yggdrasil.labs.mealmate.app.mealplan.prompt.MealPlanPromptBuilder;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatGateway;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatResult;
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

/** AI 周餐计划生成执行器单元测试。覆盖正常流程、解析重试、fallback 降级和状态守卫。 */
@ExtendWith(MockitoExtension.class)
class AiMealPlanGenerateCmdExeTest {

    @Mock private MealPlanContextBuilder contextBuilder;
    @Mock private MealPlanPromptBuilder promptBuilder;
    @Mock private AiMealPlanResultParser resultParser;
    @Mock private AiChatGateway chatGateway;
    @Mock private PromptSanitizer sanitizer;
    @Mock private WeeklyMealPlanRepository weeklyMealPlanRepository;
    @Mock private DuplicateCheckDomainService duplicateCheckDomainService;
    @Mock private WeekPlanGenerateDomainService weekPlanGenerateDomainService;

    private AiMealPlanGenerateCmdExe cmdExe;

    /** 固定周一日期。 */
    private static final LocalDate MONDAY = LocalDate.of(2026, 7, 6);

    private static final Long FAMILY_ID = 1L;
    private static final Long SAVED_PLAN_ID = 100L;

    private List<Recipe> candidateRecipes;
    private MealPlanContext mockContext;
    private List<AiMessage> mockMessages;
    private AiChatResult mockChatResult;
    private ParsedMealPlanResult mockParsedResult;

    @BeforeEach
    void setUp() {
        cmdExe =
                new AiMealPlanGenerateCmdExe(
                        contextBuilder,
                        promptBuilder,
                        resultParser,
                        chatGateway,
                        sanitizer,
                        weeklyMealPlanRepository,
                        duplicateCheckDomainService,
                        weekPlanGenerateDomainService);

        // 构建候选菜品（覆盖 MealPlanAssembler 需要的 recipeMap）
        candidateRecipes = buildCandidateRecipes();

        // 构建 mock context
        mockContext =
                MealPlanContext.builder()
                        .familySummary("成员1：成人，30-40岁，目标：均衡饮食")
                        .preferenceSummary("忌口：香菜")
                        .recipeCatalog("ID:1 番茄炒蛋\nID:2 红烧排骨")
                        .candidateIds(List.of(1L, 2L))
                        .candidateRecipes(candidateRecipes)
                        .build();

        // 构建 mock messages
        mockMessages =
                List.of(
                        new AiMessage(AiMessage.AiRole.SYSTEM, "system prompt"),
                        new AiMessage(AiMessage.AiRole.USER, "user content"));

        // 构建 mock chat result
        mockChatResult =
                AiChatResult.builder()
                        .content("{\"days\": []}")
                        .promptTokens(100)
                        .completionTokens(200)
                        .totalTokens(300)
                        .finishReason("stop")
                        .build();

        // 构建 mock parsed result（含 items 和 reasoning）
        mockParsedResult =
                ParsedMealPlanResult.builder()
                        .items(buildSampleItems())
                        .reasoning(Map.of("2026-07-06", "营养均衡搭配"))
                        .build();
    }

    // ─── 正常流程 ───

    @Test
    void execute_normalFlow_returnsAiResult() {
        // Arrange
        AiMealPlanGenerateCmd cmd = buildCmd(MONDAY);
        stubCommonMocks();
        when(resultParser.parse(anyString(), any(), eq(FAMILY_ID), eq(MONDAY)))
                .thenReturn(mockParsedResult);

        // Act
        AiMealPlanResultCO result = cmdExe.execute(cmd);

        // Assert
        assertNotNull(result);
        assertEquals(SAVED_PLAN_ID, result.getPlanId());
        assertFalse(result.isFallback());
        assertNotNull(result.getReasoning());
        assertFalse(result.getReasoning().isEmpty());
        assertEquals("AI_GENERATED", result.getPlanSource());

        verify(chatGateway, times(1)).chat(any());
        verify(weeklyMealPlanRepository).save(any());
    }

    // ─── chatGateway 异常 → fallback ───

    @Test
    void execute_chatGatewayThrows_fallsBack() {
        // Arrange
        AiMealPlanGenerateCmd cmd = buildCmd(MONDAY);
        stubContextAndSanitizer();
        when(promptBuilder.buildMessages(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(mockMessages);
        when(chatGateway.chat(any())).thenThrow(new RuntimeException("LLM timeout"));

        // fallback 路径
        WeeklyMealPlan fallbackPlan = buildSavedPlan(PlanSource.RULE_ENGINE);
        when(weekPlanGenerateDomainService.generate(eq(FAMILY_ID), eq(MONDAY), any()))
                .thenReturn(fallbackPlan);
        when(weeklyMealPlanRepository.save(any())).thenReturn(fallbackPlan);

        // Act
        AiMealPlanResultCO result = cmdExe.execute(cmd);

        // Assert
        assertTrue(result.isFallback());
        assertNotNull(result.getReasoning());
        assertTrue(result.getReasoning().isEmpty());
        assertEquals(SAVED_PLAN_ID, result.getPlanId());

        verify(weekPlanGenerateDomainService).generate(eq(FAMILY_ID), eq(MONDAY), any());
    }

    // ─── 解析首次失败，重试成功 ───

    @Test
    void execute_parseFailThenRetrySuccess_returnsAiResult() {
        // Arrange
        AiMealPlanGenerateCmd cmd = buildCmd(MONDAY);
        stubCommonMocks();

        // 第一次 parse 抛异常，第二次成功
        when(resultParser.parse(anyString(), any(), eq(FAMILY_ID), eq(MONDAY)))
                .thenThrow(new AiMealPlanParseException("bad json", new RuntimeException()))
                .thenReturn(mockParsedResult);
        // 重试需要再调一次 chatGateway
        when(chatGateway.chat(any())).thenReturn(mockChatResult);

        // Act
        AiMealPlanResultCO result = cmdExe.execute(cmd);

        // Assert
        assertFalse(result.isFallback());
        assertEquals(SAVED_PLAN_ID, result.getPlanId());

        // chatGateway 被调用 2 次（首次 + 重试）
        verify(chatGateway, times(2)).chat(any());
        // resultParser 被调用 2 次
        verify(resultParser, times(2)).parse(anyString(), any(), eq(FAMILY_ID), eq(MONDAY));
    }

    // ─── 解析两次都失败 → fallback ───

    @Test
    void execute_parseFailTwice_fallsBack() {
        // Arrange
        AiMealPlanGenerateCmd cmd = buildCmd(MONDAY);
        stubContextAndSanitizer();
        when(promptBuilder.buildMessages(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(mockMessages);
        when(chatGateway.chat(any())).thenReturn(mockChatResult);
        when(resultParser.parse(anyString(), any(), eq(FAMILY_ID), eq(MONDAY)))
                .thenThrow(new AiMealPlanParseException("bad json", new RuntimeException()));

        // fallback 路径
        WeeklyMealPlan fallbackPlan = buildSavedPlan(PlanSource.RULE_ENGINE);
        when(weekPlanGenerateDomainService.generate(eq(FAMILY_ID), eq(MONDAY), any()))
                .thenReturn(fallbackPlan);
        when(weeklyMealPlanRepository.save(any())).thenReturn(fallbackPlan);

        // Act
        AiMealPlanResultCO result = cmdExe.execute(cmd);

        // Assert
        assertTrue(result.isFallback());
        assertTrue(result.getReasoning().isEmpty());
        verify(weekPlanGenerateDomainService).generate(eq(FAMILY_ID), eq(MONDAY), any());
    }

    // ─── weekStartDate 非周一 → BizException ───

    @Test
    void execute_notMonday_throwsBizException() {
        // Wednesday
        AiMealPlanGenerateCmd cmd = buildCmd(LocalDate.of(2026, 7, 8));

        BizException ex = assertThrows(BizException.class, () -> cmdExe.execute(cmd));
        assertEquals("PLAN_WEEK_START_DATE_INVALID", ex.getErrCode());
    }

    // ─── 已有 CONFIRMED 计划 → BizException ───

    @Test
    void execute_confirmedPlan_throwsBizException() {
        // Arrange
        AiMealPlanGenerateCmd cmd = buildCmd(MONDAY);
        WeeklyMealPlan confirmed =
                WeeklyMealPlan.builder()
                        .id(50L)
                        .familyId(FAMILY_ID)
                        .status(PlanStatus.CONFIRMED)
                        .build();
        when(weeklyMealPlanRepository.findByFamilyIdAndWeekStartDateForUpdate(FAMILY_ID, MONDAY))
                .thenReturn(Optional.of(confirmed));

        // Act & Assert
        BizException ex = assertThrows(BizException.class, () -> cmdExe.execute(cmd));
        assertEquals("MEAL_PLAN_ALREADY_CONFIRMED", ex.getErrCode());

        verify(weeklyMealPlanRepository, never()).deleteItemsByPlanId(anyLong());
        verify(weeklyMealPlanRepository, never()).logicalDelete(anyLong());
    }

    // ─── 已有 DRAFT 计划 → 覆盖 ───

    @Test
    void execute_existingDraft_overwritten() {
        // Arrange
        AiMealPlanGenerateCmd cmd = buildCmd(MONDAY);
        WeeklyMealPlan draft =
                WeeklyMealPlan.builder()
                        .id(42L)
                        .familyId(FAMILY_ID)
                        .status(PlanStatus.DRAFT)
                        .build();
        when(weeklyMealPlanRepository.findByFamilyIdAndWeekStartDateForUpdate(FAMILY_ID, MONDAY))
                .thenReturn(Optional.of(draft));

        // 后续正常流程 mock
        when(contextBuilder.build(FAMILY_ID)).thenReturn(mockContext);
        when(sanitizer.sanitize(anyString())).thenReturn("");
        when(promptBuilder.buildMessages(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(mockMessages);
        when(chatGateway.chat(any())).thenReturn(mockChatResult);
        when(resultParser.parse(anyString(), any(), eq(FAMILY_ID), eq(MONDAY)))
                .thenReturn(mockParsedResult);
        when(weeklyMealPlanRepository.save(any()))
                .thenReturn(buildSavedPlan(PlanSource.AI_GENERATED));

        // Act
        AiMealPlanResultCO result = cmdExe.execute(cmd);

        // Assert：旧 DRAFT 被覆盖
        verify(weeklyMealPlanRepository).deleteItemsByPlanId(42L);
        verify(weeklyMealPlanRepository).logicalDelete(42L);
        assertNotNull(result);
        assertFalse(result.isFallback());
    }

    // ─── 辅助方法 ───

    private AiMealPlanGenerateCmd buildCmd(LocalDate weekStartDate) {
        return AiMealPlanGenerateCmd.builder()
                .familyId(FAMILY_ID)
                .weekStartDate(weekStartDate)
                .userHint("这周想吃清淡一些")
                .build();
    }

    /** 配置正常流程所需的公共 mock。 */
    private void stubCommonMocks() {
        stubContextAndSanitizer();
        when(promptBuilder.buildMessages(anyString(), anyString(), anyString(), any(), anyString()))
                .thenReturn(mockMessages);
        when(chatGateway.chat(any())).thenReturn(mockChatResult);
        when(weeklyMealPlanRepository.save(any()))
                .thenReturn(buildSavedPlan(PlanSource.AI_GENERATED));
    }

    /** 配置 contextBuilder + sanitizer + repository 无旧计划。 */
    private void stubContextAndSanitizer() {
        when(weeklyMealPlanRepository.findByFamilyIdAndWeekStartDateForUpdate(FAMILY_ID, MONDAY))
                .thenReturn(Optional.empty());
        when(contextBuilder.build(FAMILY_ID)).thenReturn(mockContext);
        when(sanitizer.sanitize(anyString())).thenReturn("这周想吃清淡一些");
    }

    /** 构建候选菜品列表（两道菜，覆盖 MealPlanAssembler 需要的 fields）。 */
    private List<Recipe> buildCandidateRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(
                Recipe.builder()
                        .id(1L)
                        .name("番茄炒蛋")
                        .cookingTimeMin(15)
                        .babyFriendly(true)
                        .weightLossFriendly(false)
                        .build());
        recipes.add(
                Recipe.builder()
                        .id(2L)
                        .name("红烧排骨")
                        .cookingTimeMin(60)
                        .babyFriendly(false)
                        .weightLossFriendly(false)
                        .build());
        return recipes;
    }

    /** 构建示例 MealPlanItem 列表（recipeId 在候选池中）。 */
    private List<MealPlanItem> buildSampleItems() {
        List<MealPlanItem> items = new ArrayList<>();
        items.add(
                MealPlanItem.builder()
                        .mealDate(MONDAY)
                        .mealType(MealType.BREAKFAST)
                        .recipeId(1L)
                        .crowdType(MealPlanCrowdType.FAMILY)
                        .sortOrder(0)
                        .build());
        items.add(
                MealPlanItem.builder()
                        .mealDate(MONDAY)
                        .mealType(MealType.LUNCH)
                        .recipeId(2L)
                        .crowdType(MealPlanCrowdType.FAMILY)
                        .sortOrder(1)
                        .build());
        return items;
    }

    /** 构建保存后的 WeeklyMealPlan（带 id 和 items）。 */
    private WeeklyMealPlan buildSavedPlan(PlanSource planSource) {
        return WeeklyMealPlan.builder()
                .id(SAVED_PLAN_ID)
                .familyId(FAMILY_ID)
                .weekStartDate(MONDAY)
                .weekEndDate(MONDAY.plusDays(6))
                .status(PlanStatus.DRAFT)
                .planSource(planSource)
                .items(buildSampleItems())
                .build();
    }
}
