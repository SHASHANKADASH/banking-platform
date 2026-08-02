package org.shashanka.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ThreadConfig {
    // If no custom async executor is configured, Spring often falls back to SimpleAsyncTaskExecutor for
    // @Async, which creates a new thread per task and does not reuse threads.
    @Bean
    @Qualifier("default")
    public Executor taskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(100);
        // by default bean takes thread name
        executor.setThreadNamePrefix("app-pool");
        executor.initialize();
        return executor;
    }

    // The reason to add separate thread pool for fraud checks is because when using request simulator, all the threads
    // were being used up, due to which application entered deadlock when trying to execute the fraud checks as no threads were left
    @Bean
    @Qualifier("fraud-check")
    public Executor fraudCheckExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(100);
        // by default bean takes thread name
        executor.setThreadNamePrefix("fraud-pool");
        executor.initialize();
        return executor;
    }
}
