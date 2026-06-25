package io.yggdrasil.labs.mealmate.domain.common.ai;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

/** AI 对话会话。通过领域方法变更状态。 */
@Getter
@Builder
public class AiSession {
    private final String sessionId;
    @Builder.Default private final List<AiMessage> messages = new ArrayList<>();
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 追加一轮对话（user + assistant） */
    public void addTurn(AiMessage userMessage, AiMessage assistantReply) {
        this.messages.add(userMessage);
        this.messages.add(assistantReply);
        this.updatedAt = LocalDateTime.now();
    }

    /** 当前对话轮次（user 消息数量） */
    public int turnCount() {
        return (int) messages.stream().filter(m -> m.getRole() == AiMessage.AiRole.USER).count();
    }

    /** 获取完整消息列表副本 */
    public List<AiMessage> allMessages() {
        return List.copyOf(messages);
    }
}
