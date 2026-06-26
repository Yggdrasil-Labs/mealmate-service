package io.yggdrasil.labs.mealmate.infrastructure.ai.session;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.domain.common.ai.AiSession;

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

    @Test
    void create_should_return_session_id_and_store() {
        AiSession session =
                AiSession.builder()
                        .messages(new ArrayList<>())
                        .createdAt(LocalDateTime.now())
                        .build();

        String id = repository.create(session);

        assertNotNull(id);
        assertFalse(id.isEmpty());
        verify(valueOps).set(eq("ai:session:" + id), anyString(), any());
    }

    @Test
    void findById_should_return_empty_when_not_exists() {
        when(valueOps.get("ai:session:not-exist")).thenReturn(null);

        Optional<AiSession> result = repository.findById("not-exist");

        assertTrue(result.isEmpty());
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

        assertTrue(result.isPresent());
        assertEquals("test-123", result.get().getSessionId());
    }

    @Test
    void delete_should_remove_key() {
        repository.delete("to-delete");
        verify(redisTemplate).delete("ai:session:to-delete");
    }
}
