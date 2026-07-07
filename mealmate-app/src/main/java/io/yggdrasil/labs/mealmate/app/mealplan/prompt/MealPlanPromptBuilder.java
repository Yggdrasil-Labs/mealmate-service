package io.yggdrasil.labs.mealmate.app.mealplan.prompt;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage;

/** 构建 AI 饮食计划生成的 LLM 消息列表。 从 classpath 加载 system prompt，拼装 [SYSTEM, USER] 两条消息。 */
@Component
public class MealPlanPromptBuilder {

    private String systemPrompt;

    @PostConstruct
    void init() {
        try {
            ClassPathResource resource =
                    new ClassPathResource("prompts/meal-plan-generate-system.txt");
            systemPrompt = resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load meal-plan-generate-system.txt", e);
        }
    }

    /** 构建 LLM messages: [SYSTEM, USER] */
    public List<AiMessage> buildMessages(
            String familySummary,
            String preferenceSummary,
            String recipeCatalog,
            LocalDate weekStartDate,
            String userHint) {
        String effectiveHint =
                (userHint == null || userHint.isBlank()) ? "无特殊要求，请根据家庭情况合理搭配" : userHint;

        String userContent =
                String.join(
                        "\n\n",
                        "## 家庭成员",
                        familySummary,
                        "## 饮食偏好与约束",
                        preferenceSummary,
                        "## 可选菜品列表",
                        recipeCatalog,
                        "## 本周起始日期",
                        weekStartDate.toString(),
                        "## 用户偏好指令",
                        effectiveHint);

        return List.of(
                new AiMessage(AiMessage.AiRole.SYSTEM, systemPrompt),
                new AiMessage(AiMessage.AiRole.USER, userContent));
    }
}
