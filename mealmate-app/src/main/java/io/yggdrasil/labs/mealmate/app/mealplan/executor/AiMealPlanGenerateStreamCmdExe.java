package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.app.mealplan.context.MealPlanContext;
import io.yggdrasil.labs.mealmate.app.mealplan.context.MealPlanContextBuilder;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.assembler.MealPlanAssembler;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AiMealPlanGenerateCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.AiMealPlanResultCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.WeeklyMealPlanCO;
import io.yggdrasil.labs.mealmate.app.mealplan.parser.AiMealPlanResultParser;
import io.yggdrasil.labs.mealmate.app.mealplan.parser.ParsedMealPlanResult;
import io.yggdrasil.labs.mealmate.app.mealplan.prompt.MealPlanPromptBuilder;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatGateway;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatRequest;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatResult;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage;
import io.yggdrasil.labs.mealmate.domain.common.ai.PromptSanitizer;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.domain.mealplan.exception.MealPlanErrorCode;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.WeeklyMealPlan;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanSource;
import io.yggdrasil.labs.mealmate.domain.mealplan.model.enums.PlanStatus;
import io.yggdrasil.labs.mealmate.domain.mealplan.repo.WeeklyMealPlanRepository;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.DuplicateCheckDomainService;
import io.yggdrasil.labs.mealmate.domain.mealplan.service.WeekPlanGenerateDomainService;
import io.yggdrasil.labs.mealmate.domain.recipe.model.Recipe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 周餐计划流式生成命令执行器。
 *
 * <p>复用同步版的 validate/context/prompt/parse/persist 逻辑，改为流式回调模式：
 *
 * <ol>
 *   <li>校验 weekStartDate 为周一
 *   <li>组装 AI 上下文（家庭画像 + 偏好 + 候选菜品）
 *   <li>构建 prompt → streamChat
 *   <li>onChunk 透传给调用方
 *   <li>onComplete 时 parse + validate + persist + onResult
 *   <li>onError 时 fallback 规则引擎 + onResult(fallback=true)
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiMealPlanGenerateStreamCmdExe {

    /** 正在处理中的 familyId+weekStartDate 组合键，用于并发互斥。 */
    private static final ConcurrentHashMap<String, Boolean> IN_FLIGHT_REQUESTS =
            new ConcurrentHashMap<>();

    private final MealPlanContextBuilder contextBuilder;
    private final MealPlanPromptBuilder promptBuilder;
    private final AiMealPlanResultParser resultParser;
    private final AiChatGateway chatGateway;
    private final PromptSanitizer sanitizer;
    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final DuplicateCheckDomainService duplicateCheckDomainService;
    private final WeekPlanGenerateDomainService weekPlanGenerateDomainService;

    /**
     * 流式执行 AI 周餐计划生成。
     *
     * @param cmd 生成命令
     * @param onChunk 每收到 LLM 增量文本时回调
     * @param onResult 流完成或 fallback 完成后回调最终结果
     * @param onError 不可恢复异常时回调
     */
    public void execute(
            AiMealPlanGenerateCmd cmd,
            Consumer<String> onChunk,
            Consumer<AiMealPlanResultCO> onResult,
            Consumer<Exception> onError) {

        // 1. weekStartDate 必须为周一
        if (cmd.getWeekStartDate().getDayOfWeek() != DayOfWeek.MONDAY) {
            onError.accept(new BizException(MealPlanErrorCode.PLAN_WEEK_START_DATE_INVALID));
            return;
        }

        // 2. 并发保护
        String requestKey = cmd.getFamilyId() + ":" + cmd.getWeekStartDate();
        if (IN_FLIGHT_REQUESTS.putIfAbsent(requestKey, Boolean.TRUE) != null) {
            onError.accept(
                    new BizException(
                            io.yggdrasil.labs.mealmate.domain.common.ai.AiErrorCode
                                    .AI_SESSION_BUSY));
            return;
        }

        try {
            // 3. 组装 AI 上下文
            MealPlanContext context = contextBuilder.build(cmd.getFamilyId());

            // 4. 清洗用户 hint
            String sanitizedHint =
                    sanitizer.sanitize(cmd.getUserHint() != null ? cmd.getUserHint() : "");

            // 5. 构建 LLM messages
            List<AiMessage> messages =
                    promptBuilder.buildMessages(
                            context.getFamilySummary(),
                            context.getPreferenceSummary(),
                            context.getRecipeCatalog(),
                            cmd.getWeekStartDate(),
                            sanitizedHint);

            // 6. 流式调用 LLM
            AiChatRequest request = buildChatRequest(messages);
            chatGateway.streamChat(
                    request,
                    new AtomicBoolean(false),
                    // onChunk：透传
                    onChunk,
                    // onComplete：解析 + 持久化 + 回调结果
                    chatResult -> {
                        try {
                            AiMealPlanResultCO result = handleComplete(cmd, context, chatResult);
                            onResult.accept(result);
                        } catch (Exception e) {
                            // AI 解析失败 → fallback 规则引擎
                            log.warn(
                                    "[Stream MealPlan] Parse failed, falling back: {}",
                                    e.getMessage());
                            try {
                                AiMealPlanResultCO fallbackResult = fallback(cmd, context);
                                onResult.accept(fallbackResult);
                            } catch (Exception fallbackEx) {
                                onError.accept(fallbackEx);
                            }
                        } finally {
                            IN_FLIGHT_REQUESTS.remove(requestKey);
                        }
                    },
                    // onError：fallback 规则引擎
                    error -> {
                        log.warn(
                                "[Stream MealPlan] Stream error, falling back: {}",
                                error.getMessage());
                        try {
                            AiMealPlanResultCO fallbackResult = fallback(cmd, context);
                            onResult.accept(fallbackResult);
                        } catch (Exception fallbackEx) {
                            onError.accept(fallbackEx);
                        } finally {
                            IN_FLIGHT_REQUESTS.remove(requestKey);
                        }
                    });
        } catch (Exception e) {
            IN_FLIGHT_REQUESTS.remove(requestKey);
            onError.accept(e);
        }
    }

    /**
     * 流完成后的业务处理：解析 AI 结果 → 标记重复 → 保存计划。
     *
     * @throws Exception 解析失败时抛出，由调用方触发 fallback
     */
    private AiMealPlanResultCO handleComplete(
            AiMealPlanGenerateCmd cmd, MealPlanContext context, AiChatResult chatResult) {

        // 解析 AI 响应
        ParsedMealPlanResult parsed =
                resultParser.parse(
                        chatResult.getContent(),
                        context,
                        cmd.getFamilyId(),
                        cmd.getWeekStartDate());

        // 标记重复菜品
        duplicateCheckDomainService.markDuplicates(parsed.getItems());

        // 构建并保存计划
        WeeklyMealPlan plan =
                WeeklyMealPlan.builder()
                        .familyId(cmd.getFamilyId())
                        .weekStartDate(cmd.getWeekStartDate())
                        .weekEndDate(cmd.getWeekStartDate().plusDays(6))
                        .status(PlanStatus.DRAFT)
                        .planSource(PlanSource.AI_GENERATED)
                        .generatedTime(LocalDateTime.now())
                        .items(parsed.getItems())
                        .build();

        WeeklyMealPlan saved = weeklyMealPlanRepository.save(plan);
        return buildResultCO(saved, context.getCandidateRecipes(), parsed.getReasoning(), false);
    }

    /** fallback 路径：规则引擎生成 → 标记重复 → 保存。 */
    private AiMealPlanResultCO fallback(AiMealPlanGenerateCmd cmd, MealPlanContext context) {
        WeeklyMealPlan plan =
                weekPlanGenerateDomainService.generate(
                        cmd.getFamilyId(), cmd.getWeekStartDate(), context.getCandidateRecipes());

        duplicateCheckDomainService.markDuplicates(plan.getItems());

        WeeklyMealPlan saved = weeklyMealPlanRepository.save(plan);
        return buildResultCO(saved, context.getCandidateRecipes(), Collections.emptyMap(), true);
    }

    /** 组装 AiMealPlanResultCO。 */
    private AiMealPlanResultCO buildResultCO(
            WeeklyMealPlan plan,
            List<Recipe> candidates,
            Map<String, String> reasoning,
            boolean fallback) {

        Map<Long, Recipe> recipeMap =
                candidates.stream()
                        .collect(Collectors.toMap(Recipe::getId, Function.identity(), (a, b) -> a));

        WeeklyMealPlanCO baseCO = MealPlanAssembler.toWeeklyMealPlanCO(plan, recipeMap);

        return AiMealPlanResultCO.builder()
                .planId(plan.getId())
                .weekStartDate(baseCO.getWeekStartDate())
                .weekEndDate(baseCO.getWeekEndDate())
                .status(baseCO.getStatus())
                .planSource(baseCO.getPlanSource())
                .dayMeals(baseCO.getDayMeals())
                .reasoning(reasoning)
                .fallback(fallback)
                .build();
    }

    /** 构建标准 AI 聊天请求。 */
    private AiChatRequest buildChatRequest(List<AiMessage> messages) {
        return AiChatRequest.builder()
                .messages(messages)
                .jsonMode(true)
                .temperature(0.7)
                .maxTokens(4096)
                .build();
    }

    /** 清除 in-flight 记录（用于测试或手动恢复）。 */
    static void clearInFlightRequests() {
        IN_FLIGHT_REQUESTS.clear();
    }
}
