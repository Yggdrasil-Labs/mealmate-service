package io.yggdrasil.labs.mealmate.domain.common.ai;

/** AI 聊天网关接口。Domain 层定义，Infrastructure 层实现。 */
public interface AiChatGateway {
    AiChatResult chat(AiChatRequest request);
}
