package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketRequiredTopicListener {

    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @KafkaListener(
            topics = "discodeit.websocket.messages",
            groupId = "discodeit-websocket-${HOSTNAME:local}",
            properties = "auto.offset.reset=latest"
    )
    public void onMessageCreatedEvent(String kafkaEvent) {
        try {
            MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

            MessageDto message = messageService.getMessage(event.messageId());

            String destination = "/sub/channels." + message.channelId() + ".messages";

            simpMessagingTemplate.convertAndSend(destination, message);

            log.info("WebSocket 메시지 전송 완료. messageId={}", event.messageId());

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("WebSocket 이벤트 역직렬화에 실패했습니다.", e);
        }
    }
}
