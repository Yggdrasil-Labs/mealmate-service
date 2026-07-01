package io.yggdrasil.labs.mealmate.domain.recipe.repo;

import java.time.Duration;
import java.util.Optional;

import io.yggdrasil.labs.mealmate.domain.recipe.model.RecipeParseCache;

/**
 * AI 菜品解析缓存仓储接口。
 *
 * <p>domain 层定义，infrastructure 层提供 Redis 实现。
 */
public interface RecipeParseCacheRepository {

    /**
     * 保存或更新解析缓存。
     *
     * @param sessionId 会话 ID
     * @param cache 解析缓存
     */
    void save(String sessionId, RecipeParseCache cache);

    /**
     * 根据会话 ID 查找解析缓存。
     *
     * @param sessionId 会话 ID
     * @return 解析缓存，不存在时返回 empty
     */
    Optional<RecipeParseCache> findBySessionId(String sessionId);

    /**
     * 更新缓存 TTL。
     *
     * @param sessionId 会话 ID
     * @param ttl 新的过期时长
     */
    void updateTtl(String sessionId, Duration ttl);
}
