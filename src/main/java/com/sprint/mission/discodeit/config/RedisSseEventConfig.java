package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.sse.RedisSseEventSubscriber;
import com.sprint.mission.discodeit.sse.RedisSseMessageRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Redis 모드에서 각 서버가 SSE 공통 토픽을 구독하도록 구성한다.
 * 메시지 처리와 오래 유지되는 구독 작업의 실행기를 분리해 서로의 작업을 막지 않게 한다.
 */
@Configuration
@ConditionalOnProperty(
        prefix = "discodeit.security.jwt",
        name = "registry-type",
        havingValue = "redis"
)
public class RedisSseEventConfig {

    /**
     * 모든 인스턴스에서 동일 토픽의 구독자를 등록한다.
     * Redis Pub/Sub은 구독자마다 전달하므로 한 서버만 소비하는 작업 큐로 사용하지 않는다.
     */
    @Bean
    public RedisMessageListenerContainer sseEventListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisSseEventSubscriber sseSubscriber,
            @Qualifier("sseEventExecutor")
            ThreadPoolTaskExecutor eventExecutor,
            @Qualifier("sseSubscriptionExecutor")
            ThreadPoolTaskExecutor subscriptionExecutor
    ){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.setTaskExecutor(eventExecutor);
        container.setSubscriptionExecutor(subscriptionExecutor);
        container.addMessageListener(sseSubscriber, new ChannelTopic(RedisSseMessageRepository.SSE_EVENTS_TOPIC));
        return container;
    }

    /**
     * 이 실행기에 들어온 메시지를 한 스레드에서 처리해 로컬 병렬 실행에 따른 순서 역전을 줄인다.
     * 네트워크 장애나 다른 발행 경로까지 포함한 전역 순서를 보장하는 설정은 아니다.
     */
    @Bean
    public ThreadPoolTaskExecutor sseEventExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("sse-event-");

        return executor;
    }

    /**
     * 구독 연결을 유지하는 작업을 메시지 처리와 다른 실행기에서 수행한다.
     */
    @Bean
    public ThreadPoolTaskExecutor sseSubscriptionExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setThreadNamePrefix("sse-subscription-");

        return executor;
    }
}
