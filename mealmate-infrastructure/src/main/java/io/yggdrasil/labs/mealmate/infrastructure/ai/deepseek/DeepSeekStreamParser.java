package io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.yggdrasil.labs.mealmate.infrastructure.ai.deepseek.dto.ChatCompletionChunk;
import lombok.extern.slf4j.Slf4j;

/**
 * SSE 流式响应解析器。
 *
 * <p>逐行读取 InputStream 中的 SSE 格式数据，解析 {@code data: } 前缀行并反序列化为 {@link ChatCompletionChunk}。支持通过
 * cancelled 标志提前终止解析循环。
 *
 * <p>解析规则：
 *
 * <ul>
 *   <li>空行、{@code event:} 行、{@code :} 开头的注释行 → 跳过
 *   <li>{@code data: [DONE]} → 调用 onDone 回调并结束循环
 *   <li>其他 {@code data: } 行 → Jackson 反序列化为 ChatCompletionChunk，成功则回调 onChunk
 *   <li>反序列化失败 → 打印警告日志，跳过该行继续处理后续数据
 * </ul>
 */
@Slf4j
@Component
public class DeepSeekStreamParser {

    private static final String DATA_PREFIX = "data: ";
    private static final String DONE_SIGNAL = "[DONE]";

    private final ObjectMapper objectMapper;

    public DeepSeekStreamParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 解析 SSE 流式响应。
     *
     * @param inputStream SSE 数据流
     * @param cancelled 取消标志，为 true 时提前终止解析
     * @param onChunk 每解析到一个有效 chunk 时的回调
     * @param onDone 收到 [DONE] 信号时的回调
     * @throws IOException 读取流时发生 I/O 错误
     */
    public void parse(
            InputStream inputStream,
            AtomicBoolean cancelled,
            Consumer<ChatCompletionChunk> onChunk,
            Runnable onDone)
            throws IOException {

        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while (!cancelled.get() && (line = reader.readLine()) != null) {
                // 跳过空行
                if (line.isEmpty()) {
                    continue;
                }

                // 跳过 SSE 注释行（以 : 开头）和 event: 行
                if (line.startsWith(":") || line.startsWith("event:")) {
                    continue;
                }

                // 只处理 data: 前缀行
                if (!line.startsWith(DATA_PREFIX)) {
                    continue;
                }

                // 提取 data: 后的内容
                String data = line.substring(DATA_PREFIX.length()).trim();

                // [DONE] 信号表示流结束
                if (DONE_SIGNAL.equals(data)) {
                    onDone.run();
                    break;
                }

                // 尝试反序列化为 ChatCompletionChunk
                try {
                    ChatCompletionChunk chunk =
                            objectMapper.readValue(data, ChatCompletionChunk.class);
                    onChunk.accept(chunk);
                } catch (Exception e) {
                    log.warn("SSE 行 JSON 解析失败，跳过该行: {}", data, e);
                }
            }
        }
    }
}
