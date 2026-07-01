package io.yggdrasil.labs.mealmate.infrastructure.ai.recipe;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParseCache;
import io.yggdrasil.labs.mealmate.domain.recipe.repo.RecipeParseCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 实现的 AI 菜品解析缓存仓储。
 *
 * <p>key 格式：ai:recipe-parsed:{sessionId}，默认 TTL 30 分钟，confirm 后 24 小时。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RedisRecipeParseCacheRepository implements RecipeParseCacheRepository {

    private static final String KEY_PREFIX = "ai:recipe-parsed:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper aiSessionMapper;

    @Override
    public void save(String sessionId, RecipeParseCache cache) {
        String json = serialize(cache);
        redisTemplate.opsForValue().set(buildKey(sessionId), json, DEFAULT_TTL);
    }

    @Override
    public Optional<RecipeParseCache> findBySessionId(String sessionId) {
        String json = redisTemplate.opsForValue().get(buildKey(sessionId));
        if (json == null) {
            return Optional.empty();
        }
        return Optional.of(deserialize(json));
    }

    @Override
    public void updateTtl(String sessionId, Duration ttl) {
        redisTemplate.expire(buildKey(sessionId), ttl);
    }

    private String buildKey(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    private String serialize(RecipeParseCache cache) {
        try {
            return aiSessionMapper.writeValueAsString(cache);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize RecipeParseCache", e);
        }
    }

    private RecipeParseCache deserialize(String json) {
        try {
            return aiSessionMapper.readValue(json, RecipeParseCache.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize RecipeParseCache", e);
        }
    }
}
