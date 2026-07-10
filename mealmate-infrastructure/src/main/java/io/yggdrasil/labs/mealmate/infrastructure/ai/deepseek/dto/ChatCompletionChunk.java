package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DeepSeek 流式响应的单个 chunk DTO。
 *
 * <p>对应 SSE data 行中的 JSON 对象，每个 chunk 包含一个或多个 choice， 其中 delta 携带增量文本内容。流结束时 finishReason 为 "stop"。
 */
@Data
@NoArgsConstructor
public class ChatCompletionChunk {

    private String id;
    private List<Choice> choices;

    /** 仅在最后一个 chunk（含 usage 信息时）出现。 */
    private ChatCompletionResponse.Usage usage;

    @Data
    @NoArgsConstructor
    public static class Choice {
        private int index;
        private Delta delta;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    public static class Delta {
        /** 增量文本内容。 */
        private String content;

        /** 增量推理内容（仅 thinking 模型）。 */
        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }
}
