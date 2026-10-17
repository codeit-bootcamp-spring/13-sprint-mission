package com.sprint.mission.discodeit.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableRetry
public class AsyncConfig {

    @Bean
    public TaskDecorator contextCopyingTaskDecorator() {
        return runnable -> {
            Map<String, String> contextMap =
                    MDC.getCopyOfContextMap();

            SecurityContext securityContext =
                    SecurityContextHolder.getContext();

            return () -> {
                try {
                    if (contextMap != null) {
                        MDC.setContextMap(contextMap);
                    } else {
                        MDC.clear();
                    }

                    SecurityContextHolder.setContext(securityContext);
                    runnable.run();
                } finally {
                    MDC.clear();
                    SecurityContextHolder.clearContext();
                }
            };
        };
    }

    @Bean(name = {"taskExecutor", "eventTaskExecutor"})
    public Executor taskExecutor(
            TaskDecorator contextCopyingTaskDecorator
    ) {
        ThreadPoolTaskExecutor executor =
                new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setTaskDecorator(contextCopyingTaskDecorator);
        executor.initialize();

        return executor;
    }
}