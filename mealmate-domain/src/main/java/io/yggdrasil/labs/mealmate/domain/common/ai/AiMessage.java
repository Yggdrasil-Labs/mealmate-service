package io.yggdrasil.labs.mealmate.domain.common.ai;

import lombok.Value;

/** AI 对话消息（不可变值对象）。 */
@Value
public class AiMessage {
    AiRole role;
    String content;

    public enum AiRole {
        SYSTEM,
        USER,
        ASSISTANT
    }
}
