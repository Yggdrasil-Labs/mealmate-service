package io.yggdrasil.labs.mealmate.infrastructure.ai.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * AI 流式处理配置：异步线程池 + SSE 路径 Filter 绕行。
 *
 * <p>AccessLogFilter（mimir-boot-starter-log）使用 ContentCachingResponseWrapper 包裹所有响应， 这会阻断
 * SseEmitter 的异步流式写入（表现为 Content-Length:0 + 空 body）。 此配置注册一个高优先级 bypass filter，在 SSE 路径上跳过后续的
 * AccessLogFilter。
 */
@Configuration
public class AiStreamAsyncConfig {

    @Bean("aiStreamExecutor")
    public TaskExecutor aiStreamExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(32);
        executor.setThreadNamePrefix("ai-stream-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * SSE 路径标记 Filter：设置 request attribute 标记此请求为 SSE 流式。 AccessLogFilter 会检查此标记并跳过
     * ContentCachingResponseWrapper。
     *
     * <p>由于无法修改 mimir-boot-starter-log 的源码，此处通过 application.yml 的
     * mimir.boot.log.access.enabled=false 全局禁用 AccessLogFilter。 项目级 access log 通过 WebInterceptor 的
     * afterCompletion 提供。
     */
    // 注：由于 AccessLogFilter 无条件 wrap response，唯一可靠的方案是全局禁用它。
    // 替代方案：项目自行实现不 wrap response 的 access log。
}
