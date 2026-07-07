package io.yggdrasil.labs.mealmate.app.mealplan.executor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.yggdrasil.labs.mealmate.app.mealplan.context.MealPlanContext;
import io.yggdrasil.labs.mealmate.app.mealplan.context.MealPlanContextBuilder;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.assembler.MealPlanAssembler;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.cmd.AiMealPlanGenerateCmd;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.AiMealPlanResultCO;
import io.yggdrasil.labs.mealmate.app.mealplan.dto.co.WeeklyMealPlanCO;
import io.yggdrasil.labs.mealmate.app.mealplan.parser.AiMealPlanParseException;
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
 * AI 周餐计划生成命令执行器。
 *
 * <p>核心编排流程：
 *
 * <ol>
 *   <li>校验 weekStartDate 为周一
 *   <li>并发互斥（SELECT FOR UPDATE）+ 覆盖已有 DRAFT
 *   <li>组装 AI 上下文（家庭画像 + 偏好 + 候选菜品）
 *   <li>调用 LLM 生成配餐方案（失败时自动重试解析 1 次）
 *   <li>AI 整体失败时 fallback 到规则引擎
 *   <li>标记重复 + 持久化 + 组装返回
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiMealPlanGenerateCmdExe {

    private final MealPlanContextBuilder contextBuilder;
    private final MealPlanPromptBuilder promptBuilder;
    private final AiMealPlanResultParser resultParser;
    private final AiChatGateway chatGateway;
    private final PromptSanitizer sanitizer;
    private final WeeklyMealPlanRepository weeklyMealPlanRepository;
    private final DuplicateCheckDomainService duplicateCheckDomainService;
    private final WeekPlanGenerateDomainService weekPlanGenerateDomainService;

    /**
     * 执行 AI 周餐计划生成。
     *
     * @param cmd 生成命令
     * @return AI 配餐结果（fallback=false 为 AI 生成，fallback=true 为规则引擎降级）
     */
    @Transactional(rollbackFor = Exception.class)
    public AiMealPlanResultCO execute(AiMealPlanGenerateCmd cmd) {
        // 1. weekStartDate 必须为周一
        if (cmd.getWeekStartDate().getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new BizException(MealPlanErrorCode.PLAN_WEEK_START_DATE_INVALID);
        }

        // 2. 并发互斥 + 检查已有计划
        Optional<WeeklyMealPlan> existing =
                weeklyMealPlanRepository.findByFamilyIdAndWeekStartDateForUpdate(
                        cmd.getFamilyId(), cmd.getWeekStartDate());
        if (existing.isPresent()) {
            WeeklyMealPlan old = existing.get();
            if (old.getStatus() == PlanStatus.CONFIRMED) {
                throw new BizException(MealPlanErrorCode.PLAN_ALREADY_CONFIRMED);
            }
            // 覆盖已有 DRAFT：先删旧条目再逻辑删除旧计划
            weeklyMealPlanRepository.deleteItemsByPlanId(old.getId());
            weeklyMealPlanRepository.logicalDelete(old.getId());
        }

        // 3. 组装 AI 上下文
        MealPlanContext context = contextBuilder.build(cmd.getFamilyId());

        // 4. 尝试 AI 生成，失败则 fallback
        try {
            return aiGenerate(cmd, context);
        } catch (Exception e) {
            log.warn("[AI MealPlan] AI generation failed, falling back: {}", e.getMessage());
            return fallback(cmd, context);
        }
    }

    /**
     * AI 生成路径：调用 LLM → 解析（重试 1 次） → 标记重复 → 保存。
     *
     * @throws AiMealPlanParseException 解析两次均失败时向上传播，触发 fallback
     */
    private AiMealPlanResultCO aiGenerate(AiMealPlanGenerateCmd cmd, MealPlanContext context) {
        // 清洗用户 hint，防止 prompt injection
        String sanitizedHint =
                sanitizer.sanitize(cmd.getUserHint() != null ? cmd.getUserHint() : "");

        // 构建 LLM messages
        List<AiMessage> messages =
                promptBuilder.buildMessages(
                        context.getFamilySummary(),
                        context.getPreferenceSummary(),
                        context.getRecipeCatalog(),
                        cmd.getWeekStartDate(),
                        sanitizedHint);

        // 调用 LLM
        AiChatResult chatResult = chatGateway.chat(buildChatRequest(messages));

        // 解析结果（失败重试 1 次）
        ParsedMealPlanResult parsed = parseWithRetry(chatResult, messages, context, cmd);

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

    /**
     * 解析 LLM 输出，首次失败时重新调用 LLM 再解析一次。
     *
     * @throws AiMealPlanParseException 两次均失败时抛出
     */
    private ParsedMealPlanResult parseWithRetry(
            AiChatResult chatResult,
            List<AiMessage> messages,
            MealPlanContext context,
            AiMealPlanGenerateCmd cmd) {
        try {
            return resultParser.parse(
                    chatResult.getContent(), context, cmd.getFamilyId(), cmd.getWeekStartDate());
        } catch (AiMealPlanParseException e) {
            log.warn("[AI MealPlan] First parse failed, retrying...");
            AiChatResult retryResult = chatGateway.chat(buildChatRequest(messages));
            return resultParser.parse(
                    retryResult.getContent(), context, cmd.getFamilyId(), cmd.getWeekStartDate());
        }
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

    /** 组装 AiMealPlanResultCO，复用 MealPlanAssembler 做日期分组。 */
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
}
