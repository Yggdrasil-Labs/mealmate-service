package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.web.client.RestClient;

import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatRequest;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiChatResult;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage;

/**
 * DeepSeek 真实 API 集成测试。手动装配，无 Spring 容器依赖。
 *
 * <p>覆盖场景：
 *
 * <ul>
 *   <li>Thinking mode（默认）— content + reasoning_content 均可能返回
 *   <li>Non-thinking mode — 直接 content 回答
 *   <li>JSON Output — 禁用 thinking + response_format=json_object
 * </ul>
 *
 * <p>仅在 DEEPSEEK_API_KEY 环境变量存在时运行。
 */
@EnabledIfEnvironmentVariable(named = "DEEPSEEK_API_KEY", matches = ".+")
class DeepSeekChatGatewayIT {

    private static DeepSeekChatGateway gateway;

    @BeforeAll
    static void setup() {
        DeepSeekProperties props = new DeepSeekProperties();
        props.setApiKey(System.getenv("DEEPSEEK_API_KEY"));
        props.setBaseUrl("https://api.deepseek.com");
        props.setModel("deepseek-v4-flash");
        props.setTimeoutSeconds(30);
        props.setMaxTokens(4096);
        props.setTemperature(0.7);

        DeepSeekConfig config = new DeepSeekConfig();
        RestClient restClient = config.deepSeekRestClient(props);
        gateway = new DeepSeekChatGateway(restClient, props);
    }

    /**
     * Thinking mode（默认）：模型先思考再回答。 content 是最终答案，reasoning_content 是思考过程。 由于 max_tokens 限制，content
     * 可能为空但 totalTokens > 0。
     */
    @Test
    void thinking_mode_should_return_content_with_tokens() {
        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(List.of(new AiMessage(AiMessage.AiRole.USER, "1+1=?")))
                        .maxTokens(100)
                        .build();

        AiChatResult result = gateway.chat(request);

        assertThat(result.getContent()).isNotNull();
        assertThat(result.getTotalTokens()).isGreaterThan(0);
        assertThat(result.getFinishReason()).isIn("stop", "length");
    }

    /** JSON Output：禁用 thinking + json_object 格式。 必须在 prompt 中包含 "json" 和示例格式。 */
    @Test
    void json_mode_should_return_valid_json() {
        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(
                                List.of(
                                        new AiMessage(
                                                AiMessage.AiRole.SYSTEM,
                                                "你只输出 JSON。输出格式: {\"answer\": \"...\"}"),
                                        new AiMessage(AiMessage.AiRole.USER, "1+1等于几？")))
                        .jsonMode(true)
                        .maxTokens(50)
                        .build();

        AiChatResult result = gateway.chat(request);

        // JSON mode 关闭 thinking，content 应直接包含 JSON
        assertThat(result.getContent()).contains("{");
        assertThat(result.getContent()).contains("answer");
        assertThat(result.getFinishReason()).isEqualTo("stop");
    }

    /** 多轮对话模拟：system + user 组合，验证上下文理解。 */
    @Test
    void multi_message_should_follow_system_instruction() {
        AiChatRequest request =
                AiChatRequest.builder()
                        .messages(
                                List.of(
                                        new AiMessage(
                                                AiMessage.AiRole.SYSTEM,
                                                "你是一个只会回答'是'或'否'的机器人。请用 JSON 格式输出: {\"reply\":"
                                                        + " \"是/否\"}"),
                                        new AiMessage(AiMessage.AiRole.USER, "地球是圆的吗？")))
                        .jsonMode(true)
                        .maxTokens(30)
                        .build();

        AiChatResult result = gateway.chat(request);

        assertThat(result.getContent()).contains("reply");
        assertThat(result.getTotalTokens()).isGreaterThan(0);
    }
}
