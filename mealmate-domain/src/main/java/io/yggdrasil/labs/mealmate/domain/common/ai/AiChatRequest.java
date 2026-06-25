package io.yggdrasil.labs.mealmate.domain.common.ai;

import java.util.List;

import lombok.Builder;
import lombok.Value;

/** AI 聊天请求（不可变值对象）。 */
@Value
@Builder
public class AiChatRequest {
    List<AiMessage> messages;
    boolean jsonMode;
    Double temperature;
    Integer maxTokens;
}
