package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import io.yggdrasil.labs.mealmate.domain.common.ai.*;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek.dto.ChatCompletionChunk;
import io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek.dto.ChatCompletionRequest;
import io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek.dto.ChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * DeepSeek LLM 网关实现。通过 RestClient 调用 OpenAI 兼容接口。
 *
 * <p>支持同步 chat 和流式 streamChat 两种调用模式：
 *
 * <ul>
 *   <li>同步模式使用 deepSeekRestClient（SimpleClientHttpRequestFactory）
 *   <li>流式模式使用 deepSeekStreamRestClient（JdkClientHttpRequestFactory）+ DeepSeekStreamParser
 * </ul>
 */
@Component
@Slf4j
public class DeepSeekChatGateway implements AiChatGateway {

    private final RestClient deepSeekRestClient;
    private final RestClient deepSeekStreamRestClient;
    private final DeepSeekStreamParser streamParser;
    private final DeepSeekProperties properties;

    public DeepSeekChatGateway(
            RestClient deepSeekRestClient,
            @Qualifier("deepSeekStreamRestClient") RestClient deepSeekStreamRestClient,
            DeepSeekStreamParser streamParser,
            DeepSeekProperties properties) {
        this.deepSeekRestClient = deepSeekRestClient;
        this.deepSeekStreamRestClient = deepSeekStreamRestClient;
        this.streamParser = streamParser;
        this.properties = properties;
    }

    @Override
    public AiChatResult chat(AiChatRequest request) {
        ChatCompletionRequest body = buildRequest(request);
        long start = System.currentTimeMillis();

        try {
            ChatCompletionResponse response =
                    deepSeekRestClient
                            .post()
                            .uri("/chat/completions")
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
            int status = e.getStatusCode().value();
            if (status == 429) {
                log.warn("[DeepSeek] RATE_LIMITED latency={}ms", latency);
            } else {
                log.error("[DeepSeek] ERROR status={} latency={}ms", status, latency);
            }
            throw mapException(e);
        } catch (ResourceAccessException e) {
            long latency = System.currentTimeMillis() - start;
            log.error("[DeepSeek] TIMEOUT latency={}ms error={}", latency, e.getMessage());
            throw new BizException(AiErrorCode.AI_SERVICE_UNAVAILABLE);
        }
    }

    /**
     * 流式聊天：构建 stream=true 请求，通过 exchange 获取原始响应流， 委托 DeepSeekStreamParser 逐行解析并回调 onChunk。
     *
     * <p>流程：buildRequest → setStream(true) → exchange → Parser.parse → 累积 content → onComplete。
     * 异常统一映射为 BizException 后回调 onError。
     */
    @Override
    public void streamChat(
            AiChatRequest request,
            AtomicBoolean cancelled,
            Consumer<String> onChunk,
            Consumer<AiChatResult> onComplete,
            Consumer<Exception> onError) {

        ChatCompletionRequest body = buildRequest(request);
        body.setStream(true);
        long start = System.currentTimeMillis();
        StringBuilder fullContent = new StringBuilder();
        // 用于收集最终 usage（仅最后一个 chunk 携带）
        final ChatCompletionResponse.Usage[] usageHolder = new ChatCompletionResponse.Usage[1];

        try {
            deepSeekStreamRestClient
                    .post()
                    .uri("/chat/completions")
                    .body(body)
                    .exchange(
                            (req, resp) -> {
                                // HTTP 错误码直接抛出，由外层 catch 统一处理
                                if (resp.getStatusCode().isError()) {
                                    throw new HttpClientErrorException(resp.getStatusCode());
                                }
                                try (InputStream is = resp.getBody()) {
                                    streamParser.parse(
                                            is,
                                            cancelled,
                                            chunk -> {
                                                if (chunk.getChoices() != null
                                                        && !chunk.getChoices().isEmpty()) {
                                                    ChatCompletionChunk.Delta delta =
                                                            chunk.getChoices().get(0).getDelta();
                                                    // 提取增量文本并回调
                                                    if (delta != null
                                                            && delta.getContent() != null
                                                            && !delta.getContent().isEmpty()) {
                                                        fullContent.append(delta.getContent());
                                                        onChunk.accept(delta.getContent());
                                                    }
                                                }
                                                // 收集 usage（仅最后一个 chunk 携带）
                                                if (chunk.getUsage() != null) {
                                                    usageHolder[0] = chunk.getUsage();
                                                }
                                            },
                                            () -> {
                                                /* onDone 由外层处理 */
                                            });
                                }
                                return null;
                            });

            // 流正常结束，构建完整结果
            long latency = System.currentTimeMillis() - start;
            ChatCompletionResponse.Usage usage = usageHolder[0];
            AiChatResult result =
                    AiChatResult.builder()
                            .content(fullContent.toString())
                            .finishReason("stop")
                            .promptTokens(usage != null ? usage.getPromptTokens() : 0)
                            .completionTokens(usage != null ? usage.getCompletionTokens() : 0)
                            .totalTokens(usage != null ? usage.getTotalTokens() : 0)
                            .build();
            log.info(
                    "[DeepSeek] stream model={} tokens={{prompt:{},completion:{},total:{}}}"
                            + " latency={}ms",
                    properties.getModel(),
                    result.getPromptTokens(),
                    result.getCompletionTokens(),
                    result.getTotalTokens(),
                    latency);
            onComplete.accept(result);

        } catch (Exception e) {
            long latency = System.currentTimeMillis() - start;
            log.error("[DeepSeek] stream ERROR latency={}ms error={}", latency, e.getMessage());
            if (e instanceof HttpStatusCodeException hsce) {
                onError.accept(mapException(hsce));
            } else if (e instanceof ResourceAccessException) {
                onError.accept(new BizException(AiErrorCode.AI_SERVICE_UNAVAILABLE));
            } else {
                onError.accept(new BizException(AiErrorCode.AI_SERVICE_UNAVAILABLE));
            }
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

        // JSON mode 需要关闭 thinking 以获得干净的 content 输出
        ChatCompletionRequest.Thinking thinking =
                request.isJsonMode() ? new ChatCompletionRequest.Thinking("disabled") : null;

        return ChatCompletionRequest.builder()
                .model(properties.getModel())
                .messages(messages)
                .responseFormat(format)
                .thinking(thinking)
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
        ChatCompletionResponse.MessageItem msg = choice.getMessage();

        // DeepSeek 思考模型可能把内容放在 reasoning_content 而非 content
        String content = "";
        if (msg != null) {
            content =
                    (msg.getContent() != null && !msg.getContent().isBlank())
                            ? msg.getContent()
                            : (msg.getReasoningContent() != null ? msg.getReasoningContent() : "");
        }

        return AiChatResult.builder()
                .content(content)
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
