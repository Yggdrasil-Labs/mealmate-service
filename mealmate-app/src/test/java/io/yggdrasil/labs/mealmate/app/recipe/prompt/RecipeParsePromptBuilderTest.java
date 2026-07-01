package io.yggdrasil.labs.mealmate.app.recipe.prompt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage.AiRole;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSession;
import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParsedData;

class RecipeParsePromptBuilderTest {

    private RecipeParsePromptBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new RecipeParsePromptBuilder();
        builder.setSystemPrompt("你是菜品录入助手。");
    }

    @Test
    void buildMessages_firstTurn_returnsSystemAndUser() {
        // Given: 空 session，无累积数据
        AiSession session =
                AiSession.builder()
                        .sessionId("test-session")
                        .createdAt(LocalDateTime.now())
                        .build();

        // When
        List<AiMessage> result = builder.buildMessages(session, null, "番茄炒蛋");

        // Then: [SYSTEM, USER] 顺序，USER content 包含用户原始输入
        assertEquals(2, result.size());
        assertEquals(AiRole.SYSTEM, result.get(0).getRole());
        assertEquals("你是菜品录入助手。", result.get(0).getContent());
        assertEquals(AiRole.USER, result.get(1).getRole());
        assertEquals("番茄炒蛋", result.get(1).getContent());
    }

    @Test
    void buildMessages_multiTurn_injectsAccumulatedSummary() {
        // Given: session 含历史 + 有累积解析结果
        AiSession session =
                AiSession.builder()
                        .sessionId("test-session")
                        .createdAt(LocalDateTime.now())
                        .build();
        session.addTurn(new AiMessage(AiRole.USER, "番茄炒蛋"), new AiMessage(AiRole.ASSISTANT, "已解析"));

        RecipeParsedData accumulated =
                RecipeParsedData.builder()
                        .name("番茄炒蛋")
                        .ingredients(
                                List.of(
                                        new RecipeParsedData.IngredientItem(
                                                "番茄", "VEGETABLE", 2.0, "个", true)))
                        .build();

        // When
        List<AiMessage> result = builder.buildMessages(session, accumulated, "先炒鸡蛋");

        // Then: SYSTEM + history(USER, ASSISTANT) + current USER = 4
        assertEquals(4, result.size());
        assertEquals(AiRole.SYSTEM, result.get(0).getRole());
        assertEquals(AiRole.USER, result.get(1).getRole());
        assertEquals("番茄炒蛋", result.get(1).getContent());
        assertEquals(AiRole.ASSISTANT, result.get(2).getRole());
        // 最后一条 USER 消息包含摘要和用户输入
        AiMessage lastMsg = result.get(3);
        assertEquals(AiRole.USER, lastMsg.getRole());
        assertTrue(lastMsg.getContent().contains("番茄炒蛋")); // 摘要中的菜名
        assertTrue(lastMsg.getContent().contains("先炒鸡蛋")); // 用户输入
        assertTrue(lastMsg.getContent().contains("当前已解析的菜品信息如下"));
    }

    @Test
    void buildMessages_preservesHistoryOrder() {
        // Given: 多轮历史
        AiSession session =
                AiSession.builder()
                        .sessionId("test-session")
                        .createdAt(LocalDateTime.now())
                        .build();
        session.addTurn(new AiMessage(AiRole.USER, "第一轮"), new AiMessage(AiRole.ASSISTANT, "回复1"));
        session.addTurn(new AiMessage(AiRole.USER, "第二轮"), new AiMessage(AiRole.ASSISTANT, "回复2"));

        // When
        List<AiMessage> result = builder.buildMessages(session, null, "第三轮");

        // Then: SYSTEM + 4条历史 + 当前 USER = 6
        assertEquals(6, result.size());
        assertEquals("第一轮", result.get(1).getContent());
        assertEquals("回复1", result.get(2).getContent());
        assertEquals("第二轮", result.get(3).getContent());
        assertEquals("回复2", result.get(4).getContent());
        assertEquals("第三轮", result.get(5).getContent());
    }

    @Test
    void buildMessages_nullSession_onlySystemAndUser() {
        List<AiMessage> result = builder.buildMessages(null, null, "红烧肉");

        assertEquals(2, result.size());
        assertEquals(AiRole.SYSTEM, result.get(0).getRole());
        assertEquals(AiRole.USER, result.get(1).getRole());
        assertEquals("红烧肉", result.get(1).getContent());
    }
}
