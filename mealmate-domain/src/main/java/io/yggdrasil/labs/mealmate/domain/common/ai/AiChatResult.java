package io.yggdrasil.labs.mealmate.domain.common.ai;

import lombok.Builder;
import lombok.Value;

/** AI 聊天结果（不可变值对象）。 */
@Value
@Builder
public class AiChatResult {
    String content;
    int promptTokens;
    int completionTokens;
    int totalTokens;
    String finishReason;
}
