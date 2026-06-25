package io.yggdrasil.labs.mealmate.domain.common.ai;

import java.util.Optional;

/** AI 对话会话仓储接口。Domain 层定义，Infrastructure 层实现。 */
public interface AiSessionRepository {
    String create(AiSession session);

    Optional<AiSession> findById(String sessionId);

    void update(AiSession session);

    void delete(String sessionId);
}
