package io.yggdrasil.labs.mealmate.app.recipe.executor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeParseChatCmd;
import io.yggdrasil.labs.mealmate.app.recipe.dto.co.AiRecipeParseResultCO;
import io.yggdrasil.labs.mealmate.app.recipe.prompt.RecipeParsePromptBuilder;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatGateway;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatRequest;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 菜品解析命令执行器。
 *
 * <p>编排多轮对话：加载 session/cache → 清洗输入 → 构建 messages → 调用 LLM → 解析 JSON → merge accumulatedParsed →
 * determineStatus → 持久化。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiRecipeParseCmdExe {

    private static final int MAX_TURNS = 10;

    private final AiChatGateway chatGateway;
    private final AiSessionRepository sessionRepository;
    private final RecipeParseCacheRepository parseCacheRepository;
    private final PromptSanitizer promptSanitizer;
    private final RecipeParsePromptBuilder promptBuilder;

    /** 宽松 JSON 解析器：忽略未知字段 */
    private final ObjectMapper jsonParser =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public AiRecipeParseResultCO execute(AiRecipeParseChatCmd cmd) {
        // 1. 加载或创建 AiSession
        AiSession session = loadOrCreateSession(cmd.getSessionId());
        String sessionId = session.getSessionId();

        // 2. 超轮次检查
        if (session.turnCount() >= MAX_TURNS) {
            return handleMaxTurns(sessionId, session);
        }

        // 3. 加载 RecipeParseCache
        RecipeParseCache cache =
                parseCacheRepository
                        .findBySessionId(sessionId)
                        .orElse(
                                RecipeParseCache.builder()
                                        .status(RecipeParseStatus.PARSING)
                                        .build());

        // 4. 清洗用户输入
        String sanitized = promptSanitizer.sanitize(cmd.getMessage());

        // 5. 构建 messages
        List<AiMessage> messages =
                promptBuilder.buildMessages(session, cache.getAccumulatedParsed(), sanitized);

        // 6. 调用 LLM
        AiChatResult chatResult =
                chatGateway.chat(
                        AiChatRequest.builder().messages(messages).jsonMode(false).build());

        // 7. 解析 JSON
        RecipeParsedData newParsed = parseJson(chatResult.getContent());
        String reply;
        if (newParsed == null) {
            // 解析失败：保留累积状态，返回错误提示
            reply = "抱歉，我没有正确理解您的描述。请尝试用更具体的方式描述菜品信息。";
            RecipeParseStatus currentStatus =
                    cache.getAccumulatedParsed() != null
                            ? determineStatus(cache.getAccumulatedParsed())
                            : RecipeParseStatus.PARSING;

            // 仍然记录本轮对话
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

        // 8. merge：非 null 字段覆盖
        RecipeParsedData merged = mergeParsed(cache.getAccumulatedParsed(), newParsed);

        // 9. determineStatus
        RecipeParseStatus status = determineStatus(merged);

        // 10. 提取 reply（从 JSON 中取或生成默认）
        reply = extractReply(chatResult.getContent(), merged, status);

        // 11. 持久化
        session.addTurn(
                new AiMessage(AiRole.USER, sanitized), new AiMessage(AiRole.ASSISTANT, reply));
        sessionRepository.update(session);
        parseCacheRepository.save(
                sessionId,
                RecipeParseCache.builder().accumulatedParsed(merged).status(status).build());

        // 12. 构建建议
        List<String> suggestions = buildSuggestions(merged, status);

        return AiRecipeParseResultCO.builder()
                .sessionId(sessionId)
                .reply(reply)
                .parsed(merged)
                .status(status)
                .suggestions(suggestions)
                .build();
    }

    /** 加载已有 session 或创建新 session。 */
    private AiSession loadOrCreateSession(String sessionId) {
        if (sessionId != null) {
            return sessionRepository
                    .findById(sessionId)
                    .orElseThrow(
                            () ->
                                    new io.yggdrasil.labs.mealmate.domain.common.exception
                                            .BizException(
                                            io.yggdrasil.labs.mealmate.domain.common.ai.AiErrorCode
                                                    .AI_SESSION_NOT_FOUND));
        }
        AiSession newSession = AiSession.builder().createdAt(LocalDateTime.now()).build();
        String newId = sessionRepository.create(newSession);
        return sessionRepository.findById(newId).orElseThrow();
    }

    /** 处理超过最大轮次的情况。 */
    private AiRecipeParseResultCO handleMaxTurns(String sessionId, AiSession session) {
        RecipeParseCache cache = parseCacheRepository.findBySessionId(sessionId).orElse(null);
        RecipeParsedData accumulated = cache != null ? cache.getAccumulatedParsed() : null;
        RecipeParseStatus status =
                accumulated != null ? determineStatus(accumulated) : RecipeParseStatus.REFINING;

        String reply;
        if (status == RecipeParseStatus.READY_TO_CONFIRM) {
            reply = "对话已达上限，当前菜品信息已足够，请确认入库。";
        } else {
            reply = "对话已达上限。当前菜品信息可能不完整，您可以直接确认入库或重新开始录入。";
            // 超轮次仍不完整时，强制设为 READY_TO_CONFIRM
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
        String jsonStr = extractJsonBlock(content);
        if (jsonStr == null) {
            log.warn("No JSON block found in LLM response");
            return null;
        }
        try {
            return jsonParser.readValue(jsonStr, RecipeParsedData.class);
        } catch (Exception e) {
            log.warn("Failed to parse extracted JSON as RecipeParsedData: {}", e.getMessage());
            return null;
        }
    }

    /** 从 AI 输出中提取 JSON 块（```json...``` 或纯 JSON）。 */
    private String extractJsonBlock(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        int start = content.indexOf("```json");
        if (start >= 0) {
            int jsonStart = content.indexOf('\n', start) + 1;
            int end = content.indexOf("```", jsonStart);
            if (end > jsonStart) {
                return content.substring(jsonStart, end).trim();
            }
        }
        start = content.indexOf("```\n");
        if (start >= 0) {
            int jsonStart = start + 4;
            int end = content.indexOf("```", jsonStart);
            if (end > jsonStart) {
                return content.substring(jsonStart, end).trim();
            }
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("{")) {
            return trimmed;
        }
        return null;
    }

    /** merge：非 null 字段覆盖。 */
    RecipeParsedData mergeParsed(RecipeParsedData old, RecipeParsedData newData) {
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

    /**
     * 判断解析状态。
     *
     * <ul>
     *   <li>name == null → PARSING
     *   <li>ingredients 为空 → REFINING
     *   <li>steps == null → REFINING（null 表示未填；空列表表示明确无步骤）
     *   <li>以上都满足 → READY_TO_CONFIRM
     * </ul>
     */
    RecipeParseStatus determineStatus(RecipeParsedData parsed) {
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
    private String extractReply(String content, RecipeParsedData parsed, RecipeParseStatus status) {
        if (content != null && !content.isBlank()) {
            // 两段式：取 ```json 之前的文字作为 reply
            int codeBlockStart = content.indexOf("```");
            if (codeBlockStart > 0) {
                String reply = content.substring(0, codeBlockStart).trim();
                if (!reply.isEmpty()) {
                    return reply;
                }
            }
            // 兜底：尝试从 JSON 中提取 reply 字段
            try {
                var node = jsonParser.readTree(content);
                if (node.has("reply") && !node.get("reply").isNull()) {
                    return node.get("reply").asText();
                }
            } catch (Exception ignored) {
                // 非 JSON 内容
            }
        }

        // 默认 reply
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
        List<String> suggestions = new ArrayList<>();
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
}
