package com.sprint.mission.discodeit.config;

import java.util.concurrent.RejectedExecutionException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean
  public ThreadPoolTaskExecutor fileUploadExecutor() {
    return createExecutor(4, 8, "file-upload-");
  }

  @Bean(name = {"notificationExecutor", "taskExecutor"})
  public ThreadPoolTaskExecutor notificationExecutor() {
    return createExecutor(2, 4, "notification-");
  }

  private ThreadPoolTaskExecutor createExecutor(
      int corePoolSize,
      int maxPoolSize,
      String threadNamePrefix
  ) {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(corePoolSize);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix(threadNamePrefix);
    executor.setTaskDecorator(new MdcSecurityContextTaskDecorator());

    executor.setRejectedExecutionHandler((task, pool) -> {
      if (pool.isShutdown()) {
        throw new RejectedExecutionException("Executor is shut down");
      }
      task.run();
    });

    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(120);
    return executor;
  }
}