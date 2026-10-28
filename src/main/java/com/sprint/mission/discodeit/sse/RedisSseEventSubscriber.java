package com.sprint.mission.discodeit.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.service.basic.MessageReader;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 각 인스턴스가 Redis Pub/Sub 이벤트를 받아 로컬 Spring 이벤트로 전환한다.
 * 저장소에서 이미 이력을 기록했으므로 다시 저장하거나 Redis로 재발행하지 않는다.
 * 구독이 끊긴 동안의 Pub/Sub 메시지는 자동 재전달되지 않으며 이력 복원은 재접속 경로가 맡는다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "discodeit.security.jwt",
        name = "registry-type",
        havingValue = "redis"
)
public class RedisSseEventSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        SseMessage sseMessage;

        try {
            sseMessage = objectMapper.readValue(message.getBody(), SseMessage.class);
        } catch (IOException e) {
            throw new IllegalStateException("Redis SSE 이벤트를 읽을 수 없습니다.", e);
        }

        eventPublisher.publishEvent(sseMessage);
    }
}
