package com.fraud.ruleengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async configuration for thread pool management.
 *
 * Configures asynchronous method execution with a custom thread pool
 * optimized for I/O-bound fraud detection tasks.
 */
@Configuration
public class AsyncConfig {

    /**
     * Thread pool executor for @Async methods.
     *
     * Configuration:
     * - Core pool size: 10 threads
     * - Max pool size: 50 threads
     * - Queue capacity: 500 tasks
     * - Thread name prefix: fraud-async-
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("fraud-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
