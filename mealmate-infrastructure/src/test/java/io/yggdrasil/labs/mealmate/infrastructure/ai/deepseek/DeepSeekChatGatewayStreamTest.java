package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import io.yggdrasil.labs.mealmate.domain.common.ai.*;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;

/**
 * DeepSeekChatGateway.streamChat 单测。
 *
 * <p>使用 WireMock 模拟 SSE 流式响应，验证：
 *
 * <ul>
 *   <li>AC1: onChunk 按序回调每个 delta.content 片段
 *   <li>AC2: onComplete 携带拼接后的完整 content + token 统计
 *   <li>AC3: 500 错误 → onError 回调 BizException(AI_SERVICE_UNAVAILABLE)
 *   <li>AC4: cancelled=true → 流式读取提前终止
 * </ul>
 */
@WireMockTest
class DeepSeekChatGatewayStreamTest {

    private DeepSeekChatGateway gateway;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wm) {
        DeepSeekProperties props = new DeepSeekProperties();
        props.setBaseUrl(wm.getHttpBaseUrl());
        props.setApiKey("test-key");
        props.setModel("deepseek-v4-flash");
        props.setTimeoutSeconds(10);
        props.setMaxTokens(100);
        props.setTemperature(0.7);

        // 同步 RestClient（chat 方法用，此测试不直接使用）
        RestClient syncClient =
                RestClient.builder()
                        .baseUrl(wm.getHttpBaseUrl())
                        .defaultHeader("Authorization", "Bearer test-key")
                        .defaultHeader("Content-Type", "application/json")
                        .build();

        // 流式 RestClient — 使用 JdkClientHttpRequestFactory 支持流式读取
        // 强制 HTTP/1.1 以兼容 WireMock（避免 HTTP/2 RST_STREAM 问题）
        HttpClient httpClient =
                HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        JdkClientHttpRequestFactory streamFactory = new JdkClientHttpRequestFactory(httpClient);
        RestClient streamClient =
                RestClient.builder()
                        .baseUrl(wm.getHttpBaseUrl())
                        .defaultHeader("Authorization", "Bearer test-key")
                        .defaultHeader("Content-Type", "application/json")
                        .requestFactory(streamFactory)
                        .messageConverters(
                                converters ->
                                        converters.add(
                                                0,
                                                new org.springframework.http.converter.json
                                                        .MappingJackson2HttpMessageConverter()))
                        .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        DeepSeekStreamParser parser = new DeepSeekStreamParser(objectMapper);

        gateway = new DeepSeekChatGateway(syncClient, streamClient, parser, props);
    }

    /**
     * AC1: 正常流式响应 → onChunk 按序回调每个 delta.content 片段。 AC2: 流式结束后 onComplete 携带拼接后的完整 content + token
     * 统计。
     */
    @Test
    void streamChat_normalFlow_callsOnChunkAndOnComplete(WireMockRuntimeInfo wm) {
        // 构造 SSE 响应：3 个内容 chunk + 1 个带 usage 的结束 chunk + [DONE]
        String sseBody =
                "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"你\"},\"finish_reason\":null}]}\n"
                    + "\n"
                    + "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"好\"},\"finish_reason\":null}]}\n"
                    + "\n"
                    + "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"世界\"},\"finish_reason\":null}]}\n"
                    + "\n"
                    + "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":10,\"completion_tokens\":3,\"total_tokens\":13}}\n"
                    + "\n"
                    + "data: [DONE]\n";

        stubFor(
                post("/chat/completions")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/event-stream")
                                        .withBody(sseBody)));

        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(List.of(new AiMessage(AiMessage.AiRole.USER, "hello")))
                        .jsonMode(false)
                        .build();

        List<String> chunks = new ArrayList<>();
        AtomicReference<AiChatResult> resultRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();
        AtomicBoolean cancelled = new AtomicBoolean(false);

        gateway.streamChat(request, cancelled, chunks::add, resultRef::set, errorRef::set);

        // AC1: onChunk 按序回调
        assertThat(chunks).containsExactly("你", "好", "世界");

        // AC2: onComplete 携带完整 content 和 token 统计
        AiChatResult result = resultRef.get();
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("你好世界");
        assertThat(result.getPromptTokens()).isEqualTo(10);
        assertThat(result.getCompletionTokens()).isEqualTo(3);
        assertThat(result.getTotalTokens()).isEqualTo(13);
        assertThat(result.getFinishReason()).isEqualTo("stop");

        // 无错误
        assertThat(errorRef.get()).isNull();
    }

    /** AC3: DeepSeek 返回 500 → onError 回调 BizException(AI_SERVICE_UNAVAILABLE)。 */
    @Test
    void streamChat_serverError_callsOnErrorWithUnavailable(WireMockRuntimeInfo wm) {
        stubFor(post("/chat/completions").willReturn(serverError()));

        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(List.of(new AiMessage(AiMessage.AiRole.USER, "hello")))
                        .jsonMode(false)
                        .build();

        List<String> chunks = new ArrayList<>();
        AtomicReference<AiChatResult> resultRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();
        AtomicBoolean cancelled = new AtomicBoolean(false);

        gateway.streamChat(request, cancelled, chunks::add, resultRef::set, errorRef::set);

        // 无 chunk 回调
        assertThat(chunks).isEmpty();
        // 无 complete 回调
        assertThat(resultRef.get()).isNull();
        // onError 回调了 BizException(AI_SERVICE_UNAVAILABLE)
        assertThat(errorRef.get()).isInstanceOf(BizException.class);
        assertThat(((BizException) errorRef.get()).getErrCode())
                .isEqualTo("AI_SERVICE_UNAVAILABLE");
    }

    /** AC4: cancelled=true 中途设置 → 流式读取提前终止，不再回调后续 chunk。 */
    @Test
    void streamChat_cancelledMidway_stopsEarly(WireMockRuntimeInfo wm) {
        // 构造多个 chunk，在第一个回调后取消
        String sseBody =
                "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"第一段\"},\"finish_reason\":null}]}\n"
                    + "\n"
                    + "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"第二段\"},\"finish_reason\":null}]}\n"
                    + "\n"
                    + "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"第三段\"},\"finish_reason\":null}]}\n"
                    + "\n"
                    + "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":5,\"completion_tokens\":3,\"total_tokens\":8}}\n"
                    + "\n"
                    + "data: [DONE]\n";

        stubFor(
                post("/chat/completions")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/event-stream")
                                        .withBody(sseBody)));

        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(List.of(new AiMessage(AiMessage.AiRole.USER, "hello")))
                        .jsonMode(false)
                        .build();

        List<String> chunks = new ArrayList<>();
        AtomicReference<AiChatResult> resultRef = new AtomicReference<>();
        AtomicReference<Exception> errorRef = new AtomicReference<>();
        AtomicBoolean cancelled = new AtomicBoolean(false);

        // 收到第一个 chunk 后设置 cancelled
        gateway.streamChat(
                request,
                cancelled,
                chunk -> {
                    chunks.add(chunk);
                    if (chunks.size() == 1) {
                        cancelled.set(true);
                    }
                },
                resultRef::set,
                errorRef::set);

        // 只有第一个 chunk 被回调（parser 在下次循环检查 cancelled 后退出）
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0)).isEqualTo("第一段");

        // onComplete 仍然被调用（包含截至取消时已累积的内容）
        assertThat(resultRef.get()).isNotNull();
        assertThat(resultRef.get().getContent()).isEqualTo("第一段");
    }

    /** 验证发送的请求体包含 stream=true。 */
    @Test
    void streamChat_sendsStreamTrueInBody(WireMockRuntimeInfo wm) {
        String sseBody =
                "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"ok\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":1,\"completion_tokens\":1,\"total_tokens\":2}}\n"
                    + "\n"
                    + "data: [DONE]\n";

        stubFor(
                post("/chat/completions")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/event-stream")
                                        .withBody(sseBody)));

        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(List.of(new AiMessage(AiMessage.AiRole.USER, "test")))
                        .jsonMode(false)
                        .build();

        AtomicBoolean cancelled = new AtomicBoolean(false);
        gateway.streamChat(request, cancelled, s -> {}, r -> {}, e -> {});

        // 验证请求体包含 "stream":true
        verify(
                postRequestedFor(urlEqualTo("/chat/completions"))
                        .withRequestBody(containing("\"stream\":true")));
    }

    /** 无 usage chunk 时 token 统计为 0。 */
    @Test
    void streamChat_noUsageChunk_tokensAreZero(WireMockRuntimeInfo wm) {
        // 没有 usage 字段的响应
        String sseBody =
                "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"hello\"},\"finish_reason\":\"stop\"}]}\n"
                    + "\n"
                    + "data: [DONE]\n";

        stubFor(
                post("/chat/completions")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "text/event-stream")
                                        .withBody(sseBody)));

        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(List.of(new AiMessage(AiMessage.AiRole.USER, "test")))
                        .jsonMode(false)
                        .build();

        AtomicReference<AiChatResult> resultRef = new AtomicReference<>();
        AtomicBoolean cancelled = new AtomicBoolean(false);

        gateway.streamChat(request, cancelled, s -> {}, resultRef::set, e -> {});

        AiChatResult result = resultRef.get();
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("hello");
        assertThat(result.getPromptTokens()).isEqualTo(0);
        assertThat(result.getCompletionTokens()).isEqualTo(0);
        assertThat(result.getTotalTokens()).isEqualTo(0);
    }
}
