package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * DeepSeek REST 客户端配置。
 *
 * <p>提供两个 RestClient Bean：
 *
 * <ul>
 *   <li>{@code deepSeekRestClient} — 同步调用，使用 SimpleClientHttpRequestFactory
 *   <li>{@code deepSeekStreamRestClient} — 流式调用，使用 JdkClientHttpRequestFactory（支持真正的流式 InputStream
 *       读取）
 * </ul>
 */
@Configuration
@EnableConfigurationProperties(DeepSeekProperties.class)
public class DeepSeekConfig {

    /** 同步 RestClient，用于非流式 chat 调用。 */
    @Bean
    public RestClient deepSeekRestClient(DeepSeekProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(factory)
                .build();
    }

    /**
     * 流式 RestClient，用于 SSE 流式 chat 调用。
     *
     * <p>使用 JdkClientHttpRequestFactory 以支持真正的流式 InputStream 读取， 避免 SimpleClientHttpRequestFactory
     * 缓冲整个响应体的问题。
     */
    @Bean
    public RestClient deepSeekStreamRestClient(DeepSeekProperties properties) {
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(properties.getTimeoutSeconds()));

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(factory)
                .build();
    }
}
