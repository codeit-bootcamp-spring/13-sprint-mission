package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.websocket.RedisWebSocketMessagePublisher;
import com.sprint.mission.discodeit.websocket.RedisWebSocketMessageSubscriber;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Redis 모드의 채팅 토픽 구독과 로컬 메시지 처리 실행기를 구성한다.
 * 모든 인스턴스가 구독해야 서로 다른 서버에 연결된 사용자도 같은 채팅을 받는다.
 */
@Configuration
@ConditionalOnProperty(
        prefix = "discodeit.security.jwt",
        name = "registry-type",
        havingValue = "redis"
)
public class RedisWebSocketConfig {

    /**
     * 모든 인스턴스에서 동일 토픽의 구독자를 등록한다.
     * Redis Pub/Sub은 구독자마다 전달하므로 한 서버만 소비하는 작업 큐로 사용하지 않는다.
     */
    @Bean
    public RedisMessageListenerContainer websocketMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisWebSocketMessageSubscriber subscriber,
            @Qualifier("websocketMessageExecutor")
            ThreadPoolTaskExecutor messageExecutor,
            @Qualifier("websocketSubscriptionExecutor")
            ThreadPoolTaskExecutor subscriptionExecutor
    ){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);
        container.setTaskExecutor(messageExecutor);
        container.setSubscriptionExecutor(subscriptionExecutor);

        container.addMessageListener(subscriber,new ChannelTopic(RedisWebSocketMessagePublisher.WEBSOCKET_MESSAGES_TOPIC));

        return container;
    }

    /**
     * 이 실행기에 들어온 메시지를 한 스레드에서 처리해 로컬 병렬 실행에 따른 순서 역전을 줄인다.
     * 네트워크 장애나 다른 발행 경로까지 포함한 전역 순서를 보장하는 설정은 아니다.
     */
    @Bean
    public ThreadPoolTaskExecutor websocketMessageExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("websocket-message-");

        return executor;
    }

    /**
     * 구독 연결을 유지하는 작업을 메시지 처리와 다른 실행기에서 수행한다.
     */
    @Bean
    public ThreadPoolTaskExecutor websocketSubscriptionExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setThreadNamePrefix("websocket-subscription-");

        return executor;
    }
}
