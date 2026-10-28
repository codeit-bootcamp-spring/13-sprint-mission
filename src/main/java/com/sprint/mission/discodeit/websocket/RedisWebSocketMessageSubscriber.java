package com.sprint.mission.discodeit.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 모든 서버 인스턴스가 같은 채팅 토픽을 구독하고 자신의 SimpleBroker에 전달한다.
 * 각 서버의 브로커는 그 서버에 연결된 채널 구독자에게만 메시지를 전송한다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "discodeit.security.jwt",
        name = "registry-type",
        havingValue = "redis"
)
public class RedisWebSocketMessageSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {

        MessageDto messageDto;

        try {
            messageDto = objectMapper.readValue(message.getBody(), MessageDto.class);
        } catch (IOException e) {
            throw new IllegalStateException("Redis WebSocket 메시지를 읽을 수 없습니다.", e);
        }

        String destination = "/sub/channels." + messageDto.channelId() + ".messages";
        messagingTemplate.convertAndSend(destination, messageDto);

    }
}
