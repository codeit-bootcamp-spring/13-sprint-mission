package com.sprint.mission.discodeit.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.message.MessageCreateEvent;
import com.sprint.mission.discodeit.websocket.WebSocketRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

    // 구독된 세션에 메세지를 전송하기 위한 객체.
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final WebSocketRegistry webSocketRegistry;

    @KafkaListener(topics = "discodeit.event.websocket")
    public void handleMessage(String event) {
        try {
            MessageCreateEvent messageEvent = objectMapper.readValue(event,MessageCreateEvent.class);
            UUID channelId = messageEvent.message().channelId();
            String url = String.format("/sub/channels.%s.messages", channelId);
            messagingTemplate.convertAndSend(url, messageEvent.message());
            log.debug("[EventConsumer] WebSocket 메세지 발송 이벤트 완료. 채널 : {}",channelId);
        } catch (JsonProcessingException e) {
            log.error("[EventConsumer] {} 이벤트 파싱 에러\nevent : {}","UserEvent",event);
            throw new RuntimeException(e);
        }



    }

}
