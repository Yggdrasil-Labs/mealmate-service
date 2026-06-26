package io.yggdrasil.labs.mealmate.infrastructure.ai.session;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSession;

/** Jackson 配置：通过 Mixin 解决 domain 类反序列化，不侵入领域模型。 */
@Configuration
public class AiSessionConfig {

    @Bean
    public ObjectMapper aiSessionMapper() {
        ObjectMapper mapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.addMixIn(AiSession.class, AiSessionMixin.class);
        mapper.addMixIn(AiMessage.class, AiMessageMixin.class);
        return mapper;
    }

    /** Mixin: 指导 Jackson 如何构造 AiSession */
    abstract static class AiSessionMixin {
        @JsonCreator
        AiSessionMixin(
                @JsonProperty("sessionId") String sessionId,
                @JsonProperty("messages") List<AiMessage> messages,
                @JsonProperty("createdAt") LocalDateTime createdAt,
                @JsonProperty("updatedAt") LocalDateTime updatedAt) {}
    }

    /** Mixin: 指导 Jackson 如何构造 AiMessage */
    abstract static class AiMessageMixin {
        @JsonCreator
        AiMessageMixin(
                @JsonProperty("role") AiMessage.AiRole role,
                @JsonProperty("content") String content) {}
    }
}
