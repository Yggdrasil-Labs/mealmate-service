package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import io.yggdrasil.labs.mealmate.domain.common.ai.*;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek.dto.ChatCompletionRequest;
import io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek.dto.ChatCompletionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** DeepSeek LLM 网关实现。通过 RestClient 调用 OpenAI 兼容接口。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeepSeekChatGateway implements AiChatGateway {

    private final RestClient deepSeekRestClient;
    private final DeepSeekProperties properties;

    @Override
    public AiChatResult chat(AiChatRequest request) {
        ChatCompletionRequest body = buildRequest(request);
        long start = System.currentTimeMillis();

        try {
            ChatCompletionResponse response =
                    deepSeekRestClient
                            .post()
                            .uri("/chat/completions")
                            .header("Authorization", "Bearer " + properties.getApiKey())
                            .body(body)
                            .retrieve()
                            .body(ChatCompletionResponse.class);

            long latency = System.currentTimeMillis() - start;
            AiChatResult result = toResult(response);
            log.info(
                    "[DeepSeek] model={} tokens={{prompt:{},completion:{},total:{}}} latency={}ms",
                    properties.getModel(),
                    result.getPromptTokens(),
                    result.getCompletionTokens(),
                    result.getTotalTokens(),
                    latency);
            return result;

        } catch (HttpStatusCodeException e) {
            long latency = System.currentTimeMillis() - start;
            log.error(
                    "[DeepSeek] ERROR status={} latency={}ms", e.getStatusCode().value(), latency);
            throw mapException(e);
        } catch (ResourceAccessException e) {
            long latency = System.currentTimeMillis() - start;
            log.error("[DeepSeek] TIMEOUT latency={}ms error={}", latency, e.getMessage());
            throw new BizException(AiErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    private ChatCompletionRequest buildRequest(AiChatRequest request) {
        List<ChatCompletionRequest.MessageItem> messages =
                request.getMessages().stream()
                        .map(
                                m ->
                                        new ChatCompletionRequest.MessageItem(
                                                m.getRole().name().toLowerCase(), m.getContent()))
                        .collect(Collectors.toList());

        ChatCompletionRequest.ResponseFormat format =
                request.isJsonMode()
                        ? new ChatCompletionRequest.ResponseFormat("json_object")
                        : null;

        return ChatCompletionRequest.builder()
                .model(properties.getModel())
                .messages(messages)
                .responseFormat(format)
                .temperature(
                        request.getTemperature() != null
                                ? request.getTemperature()
                                : properties.getTemperature())
                .maxTokens(
                        request.getMaxTokens() != null
                                ? request.getMaxTokens()
                                : properties.getMaxTokens())
                .build();
    }

    private AiChatResult toResult(ChatCompletionResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new BizException(AiErrorCode.AI_RESPONSE_INVALID);
        }
        ChatCompletionResponse.Choice choice = response.getChoices().get(0);
        ChatCompletionResponse.Usage usage = response.getUsage();
        return AiChatResult.builder()
                .content(choice.getMessage() != null ? choice.getMessage().getContent() : "")
                .finishReason(choice.getFinishReason())
                .promptTokens(usage != null ? usage.getPromptTokens() : 0)
                .completionTokens(usage != null ? usage.getCompletionTokens() : 0)
                .totalTokens(usage != null ? usage.getTotalTokens() : 0)
                .build();
    }

    private BizException mapException(HttpStatusCodeException e) {
        int status = e.getStatusCode().value();
        if (status == 401) {
            return new BizException(AiErrorCode.AI_AUTH_FAILURE);
        } else if (status == 429) {
            return new BizException(AiErrorCode.AI_RATE_LIMITED);
        } else {
            return new BizException(AiErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }
}
