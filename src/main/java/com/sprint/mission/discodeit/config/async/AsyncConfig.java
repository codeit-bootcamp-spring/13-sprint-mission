package com.sprint.mission.discodeit.config.async;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig {
    /*
    비동기 전용 config.
    ThreadPool 을 Bean 으로 등록해서 요청을 thread 로 나눈다.
     */

    private final AsyncDecorator asyncDecorator;

    @Bean
    public ThreadPoolTaskExecutor notificationExecutor() {

        // TaskExecuter 의 구현체.
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(false);
        executor.setThreadNamePrefix("[noty]");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy()); // 스레드풀 포화시 정책. 기본 4가지 있다.
        executor.setTaskDecorator(asyncDecorator);
        executor.initialize();
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor contentRelateExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(false);
        executor.setThreadNamePrefix("[noty]");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy()); // 스레드풀 포화시 정책. 기본 4가지 있다.
        executor.setTaskDecorator(asyncDecorator);
        executor.initialize();
        return executor;
    }

}
