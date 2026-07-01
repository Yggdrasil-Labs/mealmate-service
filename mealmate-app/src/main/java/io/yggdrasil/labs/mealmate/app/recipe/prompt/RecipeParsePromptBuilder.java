package io.yggdrasil.labs.mealmate.app.recipe.prompt;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage.AiRole;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSession;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParsedData;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 菜品解析 Prompt 构建器。
 *
 * <p>从 classpath 加载 system prompt 模板，构建完整 messages 列表： system + 历史消息 + 当前已解析摘要 + 用户输入。
 */
@Component
@Slf4j
public class RecipeParsePromptBuilder {

    private static final String PROMPT_RESOURCE = "prompts/recipe-parse-system.txt";
    private String systemPrompt;

    @PostConstruct
    void init() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(PROMPT_RESOURCE)) {
            if (is == null) {
                throw new IllegalStateException("Prompt resource not found: " + PROMPT_RESOURCE);
            }
            systemPrompt = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load prompt resource", e);
        }
    }

    /**
     * 构建完整消息列表。
     *
     * @param session 当前会话（含历史消息）
     * @param accumulated 累积的解析结果（可为 null）
     * @param userInput 当前用户输入（已清洗）
     * @return 消息列表 [SYSTEM, ...history, USER]
     */
    public List<AiMessage> buildMessages(
            AiSession session, RecipeParsedData accumulated, String userInput) {
        List<AiMessage> messages = new ArrayList<>();

        // 1. System prompt
        messages.add(new AiMessage(AiRole.SYSTEM, systemPrompt));

        // 2. 历史消息（跳过之前的 SYSTEM 消息）
        if (session != null) {
            session.allMessages().stream()
                    .filter(m -> m.getRole() != AiRole.SYSTEM)
                    .forEach(messages::add);
        }

        // 3. 当前用户消息（如有 accumulated，注入摘要前缀）
        String userContent = buildUserContent(accumulated, userInput);
        messages.add(new AiMessage(AiRole.USER, userContent));

        return messages;
    }

    /** 构建用户消息内容。如有累积解析结果，将摘要注入前缀。 */
    private String buildUserContent(RecipeParsedData accumulated, String userInput) {
        if (accumulated == null || accumulated.getName() == null) {
            return userInput;
        }
        String summary = buildSummary(accumulated);
        return "当前已解析的菜品信息如下，请在已有基础上补充或修改：\n" + summary + "\n\n用户补充：" + userInput;
    }

    /** 将累积解析结果生成易读摘要。 */
    private String buildSummary(RecipeParsedData data) {
        List<String> parts = new ArrayList<>();
        if (data.getName() != null) {
            parts.add("菜名：" + data.getName());
        }
        if (data.getRecipeType() != null) {
            parts.add("类型：" + data.getRecipeType());
        }
        if (data.getIngredients() != null && !data.getIngredients().isEmpty()) {
            String ingredientSummary =
                    data.getIngredients().stream()
                            .map(
                                    i ->
                                            i.getIngredientName()
                                                    + (i.getQuantity() != null
                                                            ? i.getQuantity()
                                                                    + (i.getUnit() != null
                                                                            ? i.getUnit()
                                                                            : "")
                                                            : ""))
                            .collect(Collectors.joining("、"));
            parts.add("食材：" + ingredientSummary);
        }
        if (data.getSteps() != null && !data.getSteps().isEmpty()) {
            parts.add("步骤：已有" + data.getSteps().size() + "步");
        }
        if (data.getCookingTimeMin() != null) {
            parts.add("时间：" + data.getCookingTimeMin() + "分钟");
        }
        return String.join("\n", parts);
    }

    /** 供测试注入 systemPrompt */
    void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }
}
