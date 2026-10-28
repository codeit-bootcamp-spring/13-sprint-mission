package com.sprint.mission.discodeit.websocket;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 현재 인스턴스의 SimpleBroker에 채팅 메시지를 바로 전달한다.
 * 다른 인스턴스의 WebSocket 연결에는 도달하지 않으므로 단일 서버 모드에서 사용한다.
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "discodeit.security.jwt",
        name = "registry-type",
        havingValue = "in-memory"
        ,matchIfMissing = true
)
public class InMemoryWebSocketMessagePublisher implements WebSocketMessagePublisher{

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(MessageDto message) {
        String destination = "/sub/channels." + message.channelId() + ".messages";

        messagingTemplate.convertAndSend(destination, message);
    }
}
