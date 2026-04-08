package io.yggdrasil.labs.mealmate.start.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
@ConditionalOnProperty(value = "springdoc.api-docs.enabled", havingValue = "true")
public class OpenApiConfig {

    @Bean
    public OpenAPI mealmateOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("MealMate Service API")
                                .description(
                                        "HTTP API documentation for MealMate family meal"
                                                + " planning workflows.")
                                .version("v1"));
    }

    @Bean
    public GroupedOpenApi webApi() {
        return GroupedOpenApi.builder()
                .group("web")
                .packagesToScan("io.yggdrasil.labs.mealmate.adapter.web")
                .pathsToMatch("/api/**")
                .build();
    }
}
