package io.yggdrasil.labs.mealmate.domain.common.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class AiSessionTest {

    @Test
    void addTurn_should_append_messages_and_increment_turn_count() {
        AiSession session =
                AiSession.builder()
                        .sessionId("test-id")
                        .messages(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .build();

        session.addTurn(
                new AiMessage(AiMessage.AiRole.USER, "hello"),
                new AiMessage(AiMessage.AiRole.ASSISTANT, "hi"));

        assertEquals(1, session.turnCount());
        assertEquals(2, session.allMessages().size());
    }

    @Test
    void addTurn_should_update_updatedAt() {
        AiSession session =
                AiSession.builder()
                        .sessionId("test-id")
                        .messages(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .build();

        assertNull(session.getUpdatedAt());

        session.addTurn(
                new AiMessage(AiMessage.AiRole.USER, "q"),
                new AiMessage(AiMessage.AiRole.ASSISTANT, "a"));

        assertNotNull(session.getUpdatedAt());
    }

    @Test
    void allMessages_should_return_immutable_copy() {
        AiSession session =
                AiSession.builder()
                        .sessionId("test-id")
                        .messages(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .build();

        session.addTurn(
                new AiMessage(AiMessage.AiRole.USER, "x"),
                new AiMessage(AiMessage.AiRole.ASSISTANT, "y"));

        var copy = session.allMessages();
        assertEquals(2, copy.size());
        // 确认返回的是副本而非引用
        assertThrows(
                UnsupportedOperationException.class,
                () -> copy.add(new AiMessage(AiMessage.AiRole.USER, "z")));
    }
}
