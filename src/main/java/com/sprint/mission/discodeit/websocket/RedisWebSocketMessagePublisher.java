package com.sprint.mission.discodeit.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 채팅 DTO를 JSON으로 만들어 Redis Pub/Sub에 한 번 발행한다.
 * 발행 서버도 구독자를 통해 로컬 브로커에 전달하므로 여기서 별도 로컬 전송을 하지 않는다.
 * Pub/Sub은 실시간 전파 용도이며, 채팅 이력의 영속 저장은 메시지 서비스가 담당한다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "discodeit.security.jwt",
        name = "registry-type",
        havingValue = "redis"
)
public class RedisWebSocketMessagePublisher implements WebSocketMessagePublisher{

    public static final String WEBSOCKET_MESSAGES_TOPIC = "discodeit:websocket:messages";
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publish(MessageDto message) {
        String messageJson;

        try {
            messageJson = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("WebSocket 메시지를 JSON으로 변환할 수 없습니다.", e);
        }

        redisTemplate.convertAndSend(WEBSOCKET_MESSAGES_TOPIC, messageJson);

    }
}
