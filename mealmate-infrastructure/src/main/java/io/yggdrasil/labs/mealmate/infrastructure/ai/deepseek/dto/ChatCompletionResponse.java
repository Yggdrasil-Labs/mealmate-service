package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChatCompletionResponse {
    private String id;
    private List<Choice> choices;
    private Usage usage;
    private String model;

    @Data
    @NoArgsConstructor
    public static class Choice {
        private int index;
        private MessageItem message;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    @NoArgsConstructor
    public static class MessageItem {
        private String role;
        private String content;

        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }

    @Data
    @NoArgsConstructor
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;

        @JsonProperty("completion_tokens")
        private int completionTokens;

        @JsonProperty("total_tokens")
        private int totalTokens;

        @JsonProperty("prompt_cache_hit_tokens")
        private int promptCacheHitTokens;

        @JsonProperty("prompt_cache_miss_tokens")
        private int promptCacheMissTokens;

        @JsonProperty("completion_tokens_details")
        private CompletionTokensDetails completionTokensDetails;
    }

    @Data
    @NoArgsConstructor
    public static class CompletionTokensDetails {
        @JsonProperty("reasoning_tokens")
        private int reasoningTokens;
    }
}
