package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatCompletionRequest {
    private String model;
    private List<MessageItem> messages;

    @JsonProperty("response_format")
    private ResponseFormat responseFormat;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Double temperature;

    /** 启用流式响应。null 或 false 时使用同步模式。 */
    private Boolean stream;

    /** 控制 thinking mode 开关。null 时使用模型默认值（enabled）。 */
    private Thinking thinking;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageItem {
        private String role;
        private String content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseFormat {
        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Thinking {
        private String type;
    }
}
