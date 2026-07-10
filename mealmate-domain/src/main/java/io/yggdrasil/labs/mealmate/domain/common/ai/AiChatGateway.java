package io.yggdrasil.labs.mealmate.domain.common.ai;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/** AI 聊天网关接口。Domain 层定义，Infrastructure 层实现。 */
public interface AiChatGateway {

    /** 同步聊天：发送请求并等待完整响应返回。 */
    AiChatResult chat(AiChatRequest request);

    /**
     * 流式聊天：以回调模式接收增量内容。
     *
     * @param request 聊天请求
     * @param cancelled 外部取消标志，设为 true 时实现应尽快中止流
     * @param onChunk 每收到一段增量文本时回调
     * @param onComplete 流正常结束时回调完整结果
     * @param onError 流异常时回调
     */
    default void streamChat(
            AiChatRequest request,
            AtomicBoolean cancelled,
            Consumer<String> onChunk,
            Consumer<AiChatResult> onComplete,
            Consumer<Exception> onError) {
        throw new UnsupportedOperationException("Streaming not implemented");
    }
}
