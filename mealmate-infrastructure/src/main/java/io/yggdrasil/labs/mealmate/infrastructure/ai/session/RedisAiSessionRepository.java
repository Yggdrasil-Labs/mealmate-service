package io.yggdrasil.labs.mealmate.infrastructure.ai.session;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.domain.common.ai.AiSession;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Redis 实现的 AI 会话存储。TTL 30 分钟自动过期。 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisAiSessionRepository implements AiSessionRepository {

    private static final String KEY_PREFIX = "ai:session:";
    private static final Duration TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper aiSessionMapper;

    @Override
    public String create(AiSession session) {
        String sessionId = UUID.randomUUID().toString();
        AiSession toStore =
                AiSession.builder()
                        .sessionId(sessionId)
                        .messages(session.getMessages())
                        .createdAt(
                                session.getCreatedAt() != null
                                        ? session.getCreatedAt()
                                        : LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
        save(toStore);
        return sessionId;
    }

    @Override
    public Optional<AiSession> findById(String sessionId) {
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + sessionId);
        if (json == null) {
            return Optional.empty();
        }
        return Optional.of(deserialize(json));
    }

    @Override
    public void update(AiSession session) {
        AiSession refreshed =
                AiSession.builder()
                        .sessionId(session.getSessionId())
                        .messages(session.getMessages())
                        .createdAt(session.getCreatedAt())
                        .updatedAt(LocalDateTime.now())
                        .build();
        save(refreshed);
    }

    @Override
    public void delete(String sessionId) {
        redisTemplate.delete(KEY_PREFIX + sessionId);
    }

    private void save(AiSession session) {
        String json = serialize(session);
        redisTemplate.opsForValue().set(KEY_PREFIX + session.getSessionId(), json, TTL);
    }

    private String serialize(AiSession session) {
        try {
            return aiSessionMapper.writeValueAsString(session);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize AiSession", e);
        }
    }

    private AiSession deserialize(String json) {
        try {
            return aiSessionMapper.readValue(json, AiSession.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize AiSession", e);
        }
    }
}
