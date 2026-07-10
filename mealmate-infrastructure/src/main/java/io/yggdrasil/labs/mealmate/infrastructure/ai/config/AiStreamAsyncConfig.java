package io.yggdrasil.labs.mealmate.infrastructure.ai.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * AI 流式处理异步线程池配置。
 *
 * <p>为流式 chat 回调提供独立线程池，避免阻塞主请求线程。 core=4, max=8, queue=32，拒绝策略为 CallerRunsPolicy（背压时由调用方线程执行）。
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
}
