package io.yggdrasil.labs.mealmate.start.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import io.swagger.v3.oas.models.OpenAPI;

class OpenApiConfigTest {

    @Test
    void shouldNotLoadOpenApiBeansWhenApiDocsDisabled() {
        try (AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext()) {
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(
                            new MapPropertySource(
                                    "test-properties",
                                    java.util.Map.of("springdoc.api-docs.enabled", "false")));
            context.register(OpenApiConfig.class);
            context.refresh();

            assertFalse(context.containsBean("mealmateOpenApi"));
            assertFalse(context.containsBean("webApi"));
            assertTrue(context.getBeansOfType(OpenAPI.class).isEmpty());
        }
    }

    @Test
    void shouldLoadOpenApiBeansWhenApiDocsEnabled() {
        try (AnnotationConfigApplicationContext context =
                new AnnotationConfigApplicationContext()) {
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(
                            new MapPropertySource(
                                    "test-properties",
                                    java.util.Map.of("springdoc.api-docs.enabled", "true")));
            context.register(OpenApiConfig.class);
            context.refresh();

            assertTrue(context.containsBean("mealmateOpenApi"));
            assertTrue(context.containsBean("webApi"));
            assertNotNull(context.getBean(OpenAPI.class));
        }
    }
}
