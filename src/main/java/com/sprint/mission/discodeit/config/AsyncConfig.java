package com.sprint.mission.discodeit.config;

import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig {

  private final ContextPropagatingTaskDecorator taskDecorator;

  @Bean(name = "eventTaskExecutor")
  public ThreadPoolTaskExecutor eventTaskExecutor() {
    return createExecutor("event-");
  }

  @Bean(name = "uploadTaskExecutor")
  public ThreadPoolTaskExecutor uploadTaskExecutor() {
    return createExecutor("upload-");
  }

  private ThreadPoolTaskExecutor createExecutor(String prefix) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix(prefix);

    executor.setTaskDecorator(taskDecorator);
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);

    executor.initialize();

    return executor;
  }
}
