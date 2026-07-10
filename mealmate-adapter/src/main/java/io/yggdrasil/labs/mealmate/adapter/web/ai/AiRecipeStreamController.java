package io.yggdrasil.labs.mealmate.adapter.web.ai;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.yggdrasil.labs.mealmate.app.recipe.application.AiRecipeAppService;
import io.yggdrasil.labs.mealmate.app.recipe.dto.cmd.AiRecipeParseChatCmd;
import io.yggdrasil.labs.mealmate.domain.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 菜品流式解析 Controller。
 *
 * <p>通过 SSE (Server-Sent Events) 流式输出 AI 解析结果，逐 chunk 推送到前端。 使用独立线程池 aiStreamExecutor 异步执行，避免阻塞
 * Servlet 请求线程。 客户端断连、超时或异常时通过 cancelled 标志通知上游停止生产。
 */
@Validated
@RestController
@Slf4j
@RequestMapping("/api/ai/recipes")
@Tag(name = "AI Recipe Stream", description = "SSE streaming for AI recipe parsing.")
public class AiRecipeStreamController {

    private final AiRecipeAppService aiRecipeAppService;
    private final TaskExecutor aiStreamExecutor;

    public AiRecipeStreamController(
            AiRecipeAppService aiRecipeAppService,
            @Qualifier("aiStreamExecutor") TaskExecutor aiStreamExecutor) {
        this.aiRecipeAppService = aiRecipeAppService;
        this.aiStreamExecutor = aiStreamExecutor;
    }

    /**
     * AI 流式对话解析菜品。
     *
     * <p>返回 SseEmitter，事件类型包括：
     *
     * <ul>
     *   <li>chunk: LLM 增量文本片段
     *   <li>done: 流结束标志 "[DONE]"
     *   <li>result: 完整解析结果 JSON
     *   <li>error: 错误信息 JSON（含 code 和 message）
     * </ul>
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI 流式解析菜品", description = "SSE 流式输出，逐 chunk 返回 AI 解析结果")
    public SseEmitter chatStream(@RequestBody @Valid AiRecipeParseChatCmd cmd) {
        SseEmitter emitter = new SseEmitter(60_000L);
        AtomicBoolean cancelled = new AtomicBoolean(false);

        // 客户端断连、超时、异常时标记取消
        emitter.onCompletion(() -> cancelled.set(true));
        emitter.onTimeout(() -> cancelled.set(true));
        emitter.onError(e -> cancelled.set(true));

        aiStreamExecutor.execute(
                () -> {
                    try {
                        aiRecipeAppService.chatStream(
                                cmd,
                                chunk -> {
                                    if (!cancelled.get()) {
                                        sendEvent(emitter, "chunk", chunk);
                                    }
                                },
                                result -> {
                                    sendEvent(emitter, "done", "[DONE]");
                                    sendEvent(emitter, "result", result);
                                    emitter.complete();
                                },
                                error -> {
                                    sendEvent(emitter, "error", buildErrorJson(error));
                                    emitter.complete();
                                });
                    } catch (Exception e) {
                        log.error("[AI Stream] Unexpected error in recipe chat stream", e);
                        emitter.completeWithError(e);
                    }
                });

        return emitter;
    }

    /** 安全发送 SSE 事件，客户端已断连时仅记录 debug 日志。 */
    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data));
        } catch (IOException e) {
            log.debug("[AI Stream] Client disconnected: {}", e.getMessage());
        }
    }

    /** 构建错误 JSON 字符串。BizException 提取结构化 errCode，其余使用通用码。 */
    private String buildErrorJson(Exception error) {
        String code = "AI_SERVICE_UNAVAILABLE";
        String message = error.getMessage() != null ? error.getMessage() : "AI 服务暂不可用";
        if (error instanceof BizException biz) {
            code = biz.getErrCode();
            message = biz.getMessage();
        }
        return "{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}";
    }
}
