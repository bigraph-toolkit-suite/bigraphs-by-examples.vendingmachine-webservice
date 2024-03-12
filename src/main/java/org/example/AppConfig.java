package org.example;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * A configuration class for general settings.
 *
 * @author Dominik Grzelak
 */
@Configuration
@AutoConfigureOrder(1)
public class AppConfig {

    public AppConfig() {
        System.out.println("AppConfig");
    }

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("CDOServerAsync-");
        executor.initialize();
        return executor;
    }
}