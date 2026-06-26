package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "mealmate.ai.deepseek")
@Data
public class DeepSeekProperties {
    private String baseUrl = "https://api.deepseek.com";
    private String apiKey;
    private String model = "deepseek-v4-flash";
    private int timeoutSeconds = 30;
    private int maxTokens = 4096;
    private double temperature = 0.7;
}
