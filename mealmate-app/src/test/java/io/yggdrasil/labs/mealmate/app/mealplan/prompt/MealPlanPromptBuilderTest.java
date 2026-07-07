package io.yggdrasil.labs.mealmate.app.mealplan.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage;

class MealPlanPromptBuilderTest {

    private MealPlanPromptBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new MealPlanPromptBuilder();
        ReflectionTestUtils.setField(builder, "systemPrompt", "你是 MealMate 智能饮食规划助手。");
    }

    @Test
    void buildMessages_returnsExactlyTwoMessages() {
        List<AiMessage> messages =
                builder.buildMessages(
                        "爸爸、妈妈、宝宝", "宝宝忌辣", "1-番茄炒蛋,2-清蒸鱼", LocalDate.of(2026, 7, 6), null);

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getRole()).isEqualTo(AiMessage.AiRole.SYSTEM);
        assertThat(messages.get(1).getRole()).isEqualTo(AiMessage.AiRole.USER);
    }

    @Test
    void buildMessages_userMessageContainsAllSections() {
        String familySummary = "爸爸30岁、妈妈28岁、宝宝2岁";
        String preferenceSummary = "宝宝忌辣，妈妈减脂需求";
        String recipeCatalog = "1-番茄炒蛋,2-清蒸鱼,3-小米粥";
        LocalDate weekStart = LocalDate.of(2026, 7, 6);

        List<AiMessage> messages =
                builder.buildMessages(
                        familySummary, preferenceSummary, recipeCatalog, weekStart, "多安排鱼");

        String userContent = messages.get(1).getContent();
        assertThat(userContent).contains(familySummary);
        assertThat(userContent).contains(preferenceSummary);
        assertThat(userContent).contains(recipeCatalog);
        assertThat(userContent).contains(weekStart.toString());
    }

    @Test
    void buildMessages_nullHint_usesDefaultText() {
        List<AiMessage> messages =
                builder.buildMessages("家庭信息", "偏好信息", "菜品列表", LocalDate.of(2026, 7, 6), null);

        String userContent = messages.get(1).getContent();
        assertThat(userContent).contains("无特殊要求，请根据家庭情况合理搭配");
    }

    @Test
    void buildMessages_withHint_includesHintContent() {
        List<AiMessage> messages =
                builder.buildMessages("家庭信息", "偏好信息", "菜品列表", LocalDate.of(2026, 7, 6), "清淡川菜");

        String userContent = messages.get(1).getContent();
        assertThat(userContent).contains("清淡川菜");
        assertThat(userContent).doesNotContain("无特殊要求");
    }
}
