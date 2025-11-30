package com.cognizant.paymentservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import java.util.concurrent.Executor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Configure thread pool for async compensating transactions
     * This ensures retries run in separate threads and don't block the response
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);           // Min threads
        executor.setMaxPoolSize(10);           // Max threads
        executor.setQueueCapacity(100);        // Queue size
        executor.setThreadNamePrefix("async-compensating-tx-");
        executor.initialize();
        return executor;
    }
}