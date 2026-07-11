package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;

import io.yggdrasil.labs.mealmate.domain.common.ai.*;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;

@WireMockTest
class DeepSeekChatGatewayTest {

    private DeepSeekChatGateway gateway;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wm) {
        DeepSeekProperties props = new DeepSeekProperties();
        props.setBaseUrl(wm.getHttpBaseUrl());
        props.setApiKey("test-key");
        props.setModel("deepseek-v4-flash");
        props.setTimeoutSeconds(5);
        props.setMaxTokens(100);
        props.setTemperature(0.7);

        RestClient restClient =
                RestClient.builder()
                        .baseUrl(wm.getHttpBaseUrl())
                        .requestFactory(new SimpleClientHttpRequestFactory())
                        .defaultHeader("Authorization", "Bearer test-key")
                        .defaultHeader("Content-Type", "application/json")
                        .messageConverters(
                                converters ->
                                        converters.add(
                                                0, new MappingJackson2HttpMessageConverter()))
                        .build();

        // streamRestClient 和 parser 在本测试中不使用，但构造需要
        RestClient streamRestClient =
                RestClient.builder()
                        .baseUrl(wm.getHttpBaseUrl())
                        .defaultHeader("Authorization", "Bearer test-key")
                        .defaultHeader("Content-Type", "application/json")
                        .build();

        com.fasterxml.jackson.databind.ObjectMapper objectMapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
        DeepSeekStreamParser parser = new DeepSeekStreamParser(objectMapper);

        gateway = new DeepSeekChatGateway(restClient, streamRestClient, parser, props);
    }

    @Test
    void chat_should_return_result_on_success(WireMockRuntimeInfo wm) {
        stubFor(
                post("/chat/completions")
                        .willReturn(
                                okJson(
                                        "{\"id\":\"1\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"Hi"
                                            + " there!\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":10,\"completion_tokens\":15,\"total_tokens\":25}}")));

        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(List.of(new AiMessage(AiMessage.AiRole.USER, "hello")))
                        .jsonMode(false)
                        .build();

        AiChatResult result = gateway.chat(request);

        assertThat(result.getContent()).isEqualTo("Hi there!");
        assertThat(result.getTotalTokens()).isEqualTo(25);
        assertThat(result.getFinishReason()).isEqualTo("stop");
    }

    @Test
    void chat_should_throw_auth_failure_on_401(WireMockRuntimeInfo wm) {
        stubFor(post("/chat/completions").willReturn(unauthorized()));

        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(List.of(new AiMessage(AiMessage.AiRole.USER, "hello")))
                        .jsonMode(false)
                        .build();

        assertThatThrownBy(() -> gateway.chat(request))
                .isInstanceOf(BizException.class)
                .extracting("errCode")
                .isEqualTo("AI_AUTH_FAILURE");
    }

    @Test
    void chat_should_throw_rate_limited_on_429(WireMockRuntimeInfo wm) {
        stubFor(post("/chat/completions").willReturn(status(429)));

        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(List.of(new AiMessage(AiMessage.AiRole.USER, "hello")))
                        .jsonMode(false)
                        .build();

        assertThatThrownBy(() -> gateway.chat(request))
                .isInstanceOf(BizException.class)
                .extracting("errCode")
                .isEqualTo("AI_RATE_LIMITED");
    }

    @Test
    void chat_should_throw_unavailable_on_500(WireMockRuntimeInfo wm) {
        stubFor(post("/chat/completions").willReturn(serverError()));

        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(List.of(new AiMessage(AiMessage.AiRole.USER, "hello")))
                        .jsonMode(false)
                        .build();

        assertThatThrownBy(() -> gateway.chat(request))
                .isInstanceOf(BizException.class)
                .extracting("errCode")
                .isEqualTo("AI_SERVICE_UNAVAILABLE");
    }

    @Test
    void chat_should_send_json_mode_when_requested(WireMockRuntimeInfo wm) {
        stubFor(
                post("/chat/completions")
                        .willReturn(
                                okJson(
                                        "{\"id\":\"1\",\"choices\":[{\"index\":0,\"message\":{\"role\":\"assistant\",\"content\":\"{}\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":5,\"completion_tokens\":5,\"total_tokens\":10}}")));

        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(List.of(new AiMessage(AiMessage.AiRole.USER, "parse")))
                        .jsonMode(true)
                        .build();

        gateway.chat(request);

        verify(
                postRequestedFor(urlEqualTo("/chat/completions"))
                        .withRequestBody(containing("json_object")));
    }
}
