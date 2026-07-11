package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek.dto.ChatCompletionChunk;

/**
 * DeepSeekStreamParser 单测。
 *
 * <p>使用 ByteArrayInputStream 构造固化 SSE 输入，通过列表收集回调验证解析结果。
 */
class DeepSeekStreamParserTest {

    private DeepSeekStreamParser parser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        parser = new DeepSeekStreamParser(objectMapper);
    }

    /** AC1: 正常 SSE 输入（3 个 chunk + [DONE]）→ onChunk 被调用 3 次，每次 delta.content 正确。 */
    @Test
    void parse_normalChunks_callsOnChunkForEach() throws IOException {
        // 构造 3 个 chunk + [DONE] 的 SSE 输入
        String sse =
                "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hello\"}}]}\n"
                    + "\n"
                    + "data: {\"id\":\"2\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\""
                    + " world\"}}]}\n"
                    + "\n"
                    + "data:"
                    + " {\"id\":\"3\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"!\"}}]}\n"
                    + "\n"
                    + "data: [DONE]\n";

        InputStream input = toInputStream(sse);
        AtomicBoolean cancelled = new AtomicBoolean(false);
        List<ChatCompletionChunk> chunks = new ArrayList<>();
        AtomicBoolean doneSignal = new AtomicBoolean(false);

        parser.parse(input, cancelled, chunks::add, () -> doneSignal.set(true));

        // 验证 onChunk 被调用 3 次，内容正确
        assertThat(chunks).hasSize(3);
        assertThat(chunks.get(0).getChoices().get(0).getDelta().getContent()).isEqualTo("Hello");
        assertThat(chunks.get(1).getChoices().get(0).getDelta().getContent()).isEqualTo(" world");
        assertThat(chunks.get(2).getChoices().get(0).getDelta().getContent()).isEqualTo("!");
        assertThat(doneSignal.get()).isTrue();
    }

    /** AC2: data: [DONE] → onDone 被调用，循环结束。 */
    @Test
    void parse_doneSignal_callsOnDoneAndStops() throws IOException {
        // [DONE] 后还有数据，应被忽略
        String sse =
                "data: {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"Hi\"}}]}\n"
                    + "data: [DONE]\n"
                    + "data:"
                    + " {\"id\":\"2\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"ignored\"}}]}\n";

        InputStream input = toInputStream(sse);
        AtomicBoolean cancelled = new AtomicBoolean(false);
        List<ChatCompletionChunk> chunks = new ArrayList<>();
        AtomicBoolean doneSignal = new AtomicBoolean(false);

        parser.parse(input, cancelled, chunks::add, () -> doneSignal.set(true));

        // onDone 被调用
        assertThat(doneSignal.get()).isTrue();
        // [DONE] 后的行不应被处理
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getChoices().get(0).getDelta().getContent()).isEqualTo("Hi");
    }

    /** AC3: cancelled=true → 循环提前退出，不继续读取。 */
    @Test
    void parse_cancelledTrue_exitsEarly() throws IOException {
        String sse =
                "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"first\"}}]}\n"
                    + "data:"
                    + " {\"id\":\"2\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"second\"}}]}\n"
                    + "data: [DONE]\n";

        InputStream input = toInputStream(sse);
        // 初始就已取消
        AtomicBoolean cancelled = new AtomicBoolean(true);
        List<ChatCompletionChunk> chunks = new ArrayList<>();
        AtomicBoolean doneSignal = new AtomicBoolean(false);

        parser.parse(input, cancelled, chunks::add, () -> doneSignal.set(true));

        // cancelled=true 时不应读取任何行
        assertThat(chunks).isEmpty();
        assertThat(doneSignal.get()).isFalse();
    }

    /** AC3 补充: cancelled 在中途变为 true → 部分处理后退出。 */
    @Test
    void parse_cancelledMidway_stopsAfterCurrentIteration() throws IOException {
        String sse =
                "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"first\"}}]}\n"
                    + "data:"
                    + " {\"id\":\"2\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"second\"}}]}\n"
                    + "data:"
                    + " {\"id\":\"3\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"third\"}}]}\n"
                    + "data: [DONE]\n";

        InputStream input = toInputStream(sse);
        AtomicBoolean cancelled = new AtomicBoolean(false);
        List<ChatCompletionChunk> chunks = new ArrayList<>();

        // 收到第一个 chunk 后设置 cancelled
        parser.parse(
                input,
                cancelled,
                chunk -> {
                    chunks.add(chunk);
                    if (chunks.size() == 1) {
                        cancelled.set(true);
                    }
                },
                () -> {});

        // 只有第一个 chunk 被处理后，循环在下次检查 cancelled 时退出
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getChoices().get(0).getDelta().getContent()).isEqualTo("first");
    }

    /** AC4: 无效 JSON 行 → 跳过该行不报错，继续解析后续行。 */
    @Test
    void parse_invalidJson_skipsAndContinues() throws IOException {
        String sse =
                "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"before\"}}]}\n"
                    + "data: {invalid-json-here}\n"
                    + "data: not json at all\n"
                    + "data:"
                    + " {\"id\":\"3\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"after\"}}]}\n"
                    + "data: [DONE]\n";

        InputStream input = toInputStream(sse);
        AtomicBoolean cancelled = new AtomicBoolean(false);
        List<ChatCompletionChunk> chunks = new ArrayList<>();
        AtomicBoolean doneSignal = new AtomicBoolean(false);

        // 不应抛出异常
        parser.parse(input, cancelled, chunks::add, () -> doneSignal.set(true));

        // 无效行被跳过，有效行正常解析
        assertThat(chunks).hasSize(2);
        assertThat(chunks.get(0).getChoices().get(0).getDelta().getContent()).isEqualTo("before");
        assertThat(chunks.get(1).getChoices().get(0).getDelta().getContent()).isEqualTo("after");
        assertThat(doneSignal.get()).isTrue();
    }

    /** AC5: 空行和 event: 行 → 正确忽略。 */
    @Test
    void parse_emptyAndEventLines_ignored() throws IOException {
        String sse =
                ": this is a comment\n"
                    + "event: message\n"
                    + "\n"
                    + "data:"
                    + " {\"id\":\"1\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"content\"}}]}\n"
                    + "\n"
                    + "event: done\n"
                    + ": another comment\n"
                    + "data: [DONE]\n";

        InputStream input = toInputStream(sse);
        AtomicBoolean cancelled = new AtomicBoolean(false);
        List<ChatCompletionChunk> chunks = new ArrayList<>();
        AtomicBoolean doneSignal = new AtomicBoolean(false);

        parser.parse(input, cancelled, chunks::add, () -> doneSignal.set(true));

        // 只有有效 data 行被解析，空行/event/注释行全部忽略
        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).getChoices().get(0).getDelta().getContent()).isEqualTo("content");
        assertThat(doneSignal.get()).isTrue();
    }

    /** 辅助方法：将字符串转为 InputStream。 */
    private InputStream toInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }
}
