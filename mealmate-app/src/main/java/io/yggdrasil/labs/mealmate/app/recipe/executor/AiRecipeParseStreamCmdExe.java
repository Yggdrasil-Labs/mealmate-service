package io.yggdrasil.labs.mealmate.app.recipe.executor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 菜品解析流式命令执行器。
 *
 * <p>复用同步版的 session/cache/sanitize/prompt/parse/persist 逻辑，改为流式回调模式：
 *
 * <ol>
 *   <li>并发保护：同一 sessionId 正在流式处理时拒绝新请求
 *   <li>loadSession → sanitize → buildMessages
 *   <li>streamChat：onChunk 透传、onComplete 时 parseJson+merge+persist+onResult
 * </ol>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiRecipeParseStreamCmdExe {

    private static final int MAX_TURNS = 10;

    /** 正在处理中的 sessionId 集合，用于并发互斥。 */
    private static final ConcurrentHashMap<String, Boolean> IN_FLIGHT_SESSIONS =
            new ConcurrentHashMap<>();

    private final AiChatGateway chatGateway;
    private final AiSessionRepository sessionRepository;
    private final RecipeParseCacheRepository parseCacheRepository;
    private final PromptSanitizer promptSanitizer;
    private final RecipeParsePromptBuilder promptBuilder;

    /** 宽松 JSON 解析器：忽略未知字段 */
    private final ObjectMapper jsonParser =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 流式执行菜品解析对话。
     *
     * @param cmd 对话命令
     * @param onChunk 每收到 LLM 增量文本时回调
     * @param onResult 流完成后回调完整解析结果
     * @param onError 异常时回调
     */
    public void execute(
            AiRecipeParseChatCmd cmd,
            Consumer<String> onChunk,
            Consumer<AiRecipeParseResultCO> onResult,
            Consumer<Exception> onError) {

        // 1. 加载或创建 AiSession
        AiSession session;
        try {
            session = loadOrCreateSession(cmd.getSessionId());
        } catch (Exception e) {
            onError.accept(e);
            return;
        }
        String sessionId = session.getSessionId();

        // 2. 并发保护：同一 sessionId 不允许并发流式处理
        if (IN_FLIGHT_SESSIONS.putIfAbsent(sessionId, Boolean.TRUE) != null) {
            onError.accept(new BizException(AiErrorCode.AI_SESSION_BUSY));
            return;
        }

        try {
            // 3. 超轮次检查
            if (session.turnCount() >= MAX_TURNS) {
                AiRecipeParseResultCO result = handleMaxTurns(sessionId);
                onResult.accept(result);
                return;
            }

            // 4. 加载 RecipeParseCache
            RecipeParseCache cache =
                    parseCacheRepository
                            .findBySessionId(sessionId)
                            .orElse(
                                    RecipeParseCache.builder()
                                            .status(RecipeParseStatus.PARSING)
                                            .build());

            // 5. 清洗用户输入
            String sanitized = promptSanitizer.sanitize(cmd.getMessage());

            // 6. 构建 messages
            List<AiMessage> messages =
                    promptBuilder.buildMessages(session, cache.getAccumulatedParsed(), sanitized);

            // 7. 流式调用 LLM
            AiChatRequest request =
                    AiChatRequest.builder().messages(messages).jsonMode(true).build();

            chatGateway.streamChat(
                    request,
                    new AtomicBoolean(false),
                    // onChunk：透传给调用方
                    onChunk,
                    // onComplete：解析完整响应、merge、持久化、回调结果
                    chatResult -> {
                        try {
                            AiRecipeParseResultCO result =
                                    handleComplete(
                                            sessionId, session, cache, sanitized, chatResult);
                            onResult.accept(result);
                        } catch (Exception e) {
                            onError.accept(e);
                        } finally {
                            IN_FLIGHT_SESSIONS.remove(sessionId);
                        }
                    },
                    // onError：移除 in-flight 标记，透传错误
                    error -> {
                        IN_FLIGHT_SESSIONS.remove(sessionId);
                        onError.accept(error);
                    });
        } catch (Exception e) {
            IN_FLIGHT_SESSIONS.remove(sessionId);
            onError.accept(e);
        }
    }

    /**
     * 流完成后的业务处理：parseJson → merge → determineStatus → persist → 构建结果。
     *
     * <p>JSON 解析失败时保留已有 cache，不中断流程。
     */
    private AiRecipeParseResultCO handleComplete(
            String sessionId,
            AiSession session,
            RecipeParseCache cache,
            String sanitized,
            AiChatResult chatResult) {

        RecipeParsedData newParsed = parseJson(chatResult.getContent());

        if (newParsed == null) {
            // JSON 解析失败：保留累积状态，返回已有 cache
            String reply = "抱歉，我没有正确理解您的描述。请尝试用更具体的方式描述菜品信息。";
            RecipeParseStatus currentStatus =
                    cache.getAccumulatedParsed() != null
                            ? determineStatus(cache.getAccumulatedParsed())
                            : RecipeParseStatus.PARSING;

            session.addTurn(
                    new AiMessage(AiRole.USER, sanitized), new AiMessage(AiRole.ASSISTANT, reply));
            sessionRepository.update(session);

            return AiRecipeParseResultCO.builder()
                    .sessionId(sessionId)
                    .reply(reply)
                    .parsed(cache.getAccumulatedParsed())
                    .status(currentStatus)
                    .suggestions(List.of("请用更具体的描述重试"))
                    .build();
        }

        // merge + status + persist
        RecipeParsedData merged = mergeParsed(cache.getAccumulatedParsed(), newParsed);
        RecipeParseStatus status = determineStatus(merged);
        String reply = extractReply(chatResult.getContent(), status);

        session.addTurn(
                new AiMessage(AiRole.USER, sanitized), new AiMessage(AiRole.ASSISTANT, reply));
        sessionRepository.update(session);
        parseCacheRepository.save(
                sessionId,
                RecipeParseCache.builder().accumulatedParsed(merged).status(status).build());

        return AiRecipeParseResultCO.builder()
                .sessionId(sessionId)
                .reply(reply)
                .parsed(merged)
                .status(status)
                .suggestions(buildSuggestions(merged, status))
                .build();
    }

    /** 加载已有 session 或创建新 session。 */
    private AiSession loadOrCreateSession(String sessionId) {
        if (sessionId != null) {
            return sessionRepository
                    .findById(sessionId)
                    .orElseThrow(() -> new BizException(AiErrorCode.AI_SESSION_NOT_FOUND));
        }
        AiSession newSession = AiSession.builder().createdAt(java.time.LocalDateTime.now()).build();
        String newId = sessionRepository.create(newSession);
        return sessionRepository.findById(newId).orElseThrow();
    }

    /** 处理超过最大轮次的情况。 */
    private AiRecipeParseResultCO handleMaxTurns(String sessionId) {
        RecipeParseCache cache = parseCacheRepository.findBySessionId(sessionId).orElse(null);
        RecipeParsedData accumulated = cache != null ? cache.getAccumulatedParsed() : null;
        RecipeParseStatus status =
                accumulated != null ? determineStatus(accumulated) : RecipeParseStatus.REFINING;

        String reply;
        if (status == RecipeParseStatus.READY_TO_CONFIRM) {
            reply = "对话已达上限，当前菜品信息已足够，请确认入库。";
        } else {
            reply = "对话已达上限。当前菜品信息可能不完整，您可以直接确认入库或重新开始录入。";
            status = RecipeParseStatus.READY_TO_CONFIRM;
        }

        return AiRecipeParseResultCO.builder()
                .sessionId(sessionId)
                .reply(reply)
                .parsed(accumulated)
                .status(status)
                .suggestions(List.of("确认入库", "重新开始"))
                .build();
    }

    /** 宽松解析 JSON。失败返回 null。 */
    private RecipeParsedData parseJson(String content) {
        try {
            return jsonParser.readValue(content, RecipeParsedData.class);
        } catch (Exception e) {
            log.warn(
                    "[Stream] Failed to parse LLM response as RecipeParsedData: {}",
                    e.getMessage());
            return null;
        }
    }

    /** merge：非 null 字段覆盖。 */
    private RecipeParsedData mergeParsed(RecipeParsedData old, RecipeParsedData newData) {
        if (old == null) {
            return newData;
        }
        if (newData == null) {
            return old;
        }
        return RecipeParsedData.builder()
                .name(newData.getName() != null ? newData.getName() : old.getName())
                .recipeType(
                        newData.getRecipeType() != null
                                ? newData.getRecipeType()
                                : old.getRecipeType())
                .seasonTag(
                        newData.getSeasonTag() != null
                                ? newData.getSeasonTag()
                                : old.getSeasonTag())
                .crowdTag(newData.getCrowdTag() != null ? newData.getCrowdTag() : old.getCrowdTag())
                .tasteTags(
                        newData.getTasteTags() != null
                                ? newData.getTasteTags()
                                : old.getTasteTags())
                .difficultyLevel(
                        newData.getDifficultyLevel() != null
                                ? newData.getDifficultyLevel()
                                : old.getDifficultyLevel())
                .cookingTimeMin(
                        newData.getCookingTimeMin() != null
                                ? newData.getCookingTimeMin()
                                : old.getCookingTimeMin())
                .babyFriendly(
                        newData.getBabyFriendly() != null
                                ? newData.getBabyFriendly()
                                : old.getBabyFriendly())
                .weightLossFriendly(
                        newData.getWeightLossFriendly() != null
                                ? newData.getWeightLossFriendly()
                                : old.getWeightLossFriendly())
                .ingredients(
                        newData.getIngredients() != null
                                ? newData.getIngredients()
                                : old.getIngredients())
                .steps(newData.getSteps() != null ? newData.getSteps() : old.getSteps())
                .nutritionFact(
                        newData.getNutritionFact() != null
                                ? newData.getNutritionFact()
                                : old.getNutritionFact())
                .build();
    }

    /** 判断解析状态。 */
    private RecipeParseStatus determineStatus(RecipeParsedData parsed) {
        if (parsed == null || parsed.getName() == null || parsed.getName().isBlank()) {
            return RecipeParseStatus.PARSING;
        }
        if (parsed.getIngredients() == null || parsed.getIngredients().isEmpty()) {
            return RecipeParseStatus.REFINING;
        }
        if (parsed.getSteps() == null) {
            return RecipeParseStatus.REFINING;
        }
        return RecipeParseStatus.READY_TO_CONFIRM;
    }

    /** 从 LLM 响应中提取 reply 字段，或生成默认回复。 */
    private String extractReply(String content, RecipeParseStatus status) {
        try {
            var node = jsonParser.readTree(content);
            if (node.has("reply") && !node.get("reply").isNull()) {
                return node.get("reply").asText();
            }
        } catch (Exception ignored) {
            // 解析失败则使用默认
        }
        if (status == RecipeParseStatus.READY_TO_CONFIRM) {
            return "菜品信息已完整，请确认入库。";
        }
        return "已解析部分信息，还需要补充更多细节。";
    }

    /** 根据当前状态构建补充建议。 */
    private List<String> buildSuggestions(RecipeParsedData parsed, RecipeParseStatus status) {
        if (status == RecipeParseStatus.READY_TO_CONFIRM) {
            return List.of();
        }
        java.util.ArrayList<String> suggestions = new java.util.ArrayList<>();
        if (parsed.getSteps() == null) {
            suggestions.add("补充烹饪步骤");
        }
        if (parsed.getNutritionFact() == null) {
            suggestions.add("补充营养信息");
        }
        if (parsed.getCrowdTag() == null) {
            suggestions.add("指定适用人群");
        }
        if (parsed.getSeasonTag() == null) {
            suggestions.add("指定适用季节");
        }
        return suggestions;
    }

    /** 清除 in-flight 记录（用于测试或手动恢复）。 */
    static void clearInFlightSessions() {
        IN_FLIGHT_SESSIONS.clear();
    }
}
