package com.sprint.mission.discodeit.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = {"taskExecutor", "eventTaskExecutor"})
  public TaskExecutor taskExecutor(TaskDecorator contextTaskDecorator) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("async-");
    executor.setTaskDecorator(contextTaskDecorator);
    return executor;
  }

  @Bean
  public TaskDecorator contextTaskDecorator() {
    return task -> {
      Map<String, String> callerMdc = MDC.getCopyOfContextMap();
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      return () -> {
        Map<String, String> previousMdc = MDC.getCopyOfContextMap();
        SecurityContext previousContext = SecurityContextHolder.getContext();
        // Use a separate context so async changes do not alter the caller's context.
        SecurityContext taskContext = SecurityContextHolder.createEmptyContext();
        taskContext.setAuthentication(authentication);
        try {
          setMdc(callerMdc);
          SecurityContextHolder.setContext(taskContext);
          task.run();
        } finally {
          setMdc(previousMdc);
          SecurityContextHolder.setContext(previousContext);
        }
      };
    };
  }

  private static void setMdc(Map<String, String> contextMap) {
    if (contextMap == null) {
      MDC.clear();
    } else {
      MDC.setContextMap(contextMap);
    }
  }
}
