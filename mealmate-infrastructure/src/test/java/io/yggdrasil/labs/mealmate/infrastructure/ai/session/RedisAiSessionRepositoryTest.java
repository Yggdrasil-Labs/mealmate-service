package io.yggdrasil.labs.mealmate.infrastructure.ai.session;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.domain.common.ai.AiMessage;
import io.yggdrasil.labs.mealmate.domain.common.ai.AiSession;

/**
 * Redis AI 会话存储单元测试。
 *
 * <p>使用 Mockito 模拟 StringRedisTemplate，通过 ArgumentCaptor 验证：
 *
 * <ul>
 *   <li>key 前缀、TTL 时长、JSON 序列化/反序列化 roundtrip
 *   <li>create / findById / update / delete 完整 CRUD 行为
 *   <li>update 时刷新 updatedAt 时间戳
 * </ul>
 *
 * <p>如需真实 Redis 集成测试（TTL 过期验证等），在 Docker 可用环境中启用 {@code @Testcontainers} + {@code
 * GenericContainer<RedisContainer>}。
 */
@ExtendWith(MockitoExtension.class)
class RedisAiSessionRepositoryTest {

    private RedisAiSessionRepository repository;

    @Mock private StringRedisTemplate redisTemplate;

    @Mock private ValueOperations<String, String> valueOps;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        mapper = new AiSessionConfig().aiSessionMapper();
        repository = new RedisAiSessionRepository(redisTemplate, mapper);
    }

    private static AiSession buildSession() {
        return AiSession.builder()
                .messages(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void create_should_return_non_empty_id() {
        String id = repository.create(buildSession());
        assertNotNull(id);
        assertFalse(id.isEmpty());
    }

    @Test
    void create_should_store_with_correct_key_prefix() {
        repository.create(buildSession());
        verify(valueOps).set(startsWith("ai:session:"), anyString(), any());
    }

    @Test
    void create_should_store_with_30min_ttl() {
        repository.create(buildSession());

        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOps).set(anyString(), anyString(), ttlCaptor.capture());
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void create_and_findById_should_roundtrip() {
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);

        repository.create(buildSession());

        verify(valueOps).set(keyCaptor.capture(), jsonCaptor.capture(), any());

        String key = keyCaptor.getValue();
        when(valueOps.get(key)).thenReturn(jsonCaptor.getValue());

        String sessionId = key.substring("ai:session:".length());
        Optional<AiSession> found = repository.findById(sessionId);

        assertThat(found).isPresent();
        assertThat(found.get().getSessionId()).isEqualTo(sessionId);
    }

    @Test
    void findById_should_return_empty_when_not_exists() {
        when(valueOps.get("ai:session:not-exist")).thenReturn(null);
        assertThat(repository.findById("not-exist")).isEmpty();
    }

    @Test
    void findById_should_deserialize_stored_session() throws Exception {
        AiSession original =
                AiSession.builder()
                        .sessionId("test-123")
                        .messages(new ArrayList<>())
                        .createdAt(LocalDateTime.of(2026, 6, 26, 8, 0))
                        .updatedAt(LocalDateTime.of(2026, 6, 26, 8, 0))
                        .build();
        String json = mapper.writeValueAsString(original);
        when(valueOps.get("ai:session:test-123")).thenReturn(json);

        Optional<AiSession> result = repository.findById("test-123");

        assertThat(result).isPresent();
        AiSession found = result.get();
        assertThat(found.getSessionId()).isEqualTo("test-123");
        assertThat(found.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 6, 26, 8, 0));
    }

    @Test
    void delete_should_remove_key() {
        repository.delete("to-delete");
        verify(redisTemplate).delete("ai:session:to-delete");
    }

    @Test
    void update_should_refresh_updatedAt_and_ttl() throws Exception {
        AiSession original =
                AiSession.builder()
                        .sessionId("update-test")
                        .messages(new ArrayList<>())
                        .createdAt(LocalDateTime.of(2026, 6, 26, 8, 0))
                        .updatedAt(LocalDateTime.of(2026, 6, 26, 8, 0))
                        .build();
        original.addTurn(
                new AiMessage(AiMessage.AiRole.USER, "hello"),
                new AiMessage(AiMessage.AiRole.ASSISTANT, "hi"));

        repository.update(original);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOps, atLeastOnce())
                .set(eq("ai:session:update-test"), jsonCaptor.capture(), ttlCaptor.capture());

        AiSession stored = mapper.readValue(jsonCaptor.getValue(), AiSession.class);

        assertThat(stored.getUpdatedAt()).isNotNull();
        assertThat(stored.getUpdatedAt()).isAfter(original.getUpdatedAt());
        assertThat(ttlCaptor.getValue()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void messages_should_include_system_prompt() throws Exception {
        AiSession session =
                AiSession.builder()
                        .messages(
                                new ArrayList<>(
                                        List.of(
                                                new AiMessage(
                                                        AiMessage.AiRole.SYSTEM,
                                                        "You are a chef."))))
                        .createdAt(LocalDateTime.now())
                        .build();

        repository.create(session);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOps).set(anyString(), jsonCaptor.capture(), any());

        AiSession stored = mapper.readValue(jsonCaptor.getValue(), AiSession.class);
        assertThat(stored.allMessages()).hasSize(1);
        assertThat(stored.allMessages().get(0).getRole()).isEqualTo(AiMessage.AiRole.SYSTEM);
        assertThat(stored.allMessages().get(0).getContent()).isEqualTo("You are a chef.");
    }
}
