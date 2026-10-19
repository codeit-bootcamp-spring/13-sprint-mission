package com.sprint.mission.discodeit.event.listener;

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

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketMessageTopicListener {

  private final ObjectMapper objectMapper;
  private final MessageService messageService;
  private final SimpMessagingTemplate messagingTemplate;

  @KafkaListener(
      topics = "discodeit.MessageCreatedEvent",
      groupId = "${discodeit.kafka.websocket-group-id:discodeit-ws-local}"
  )
  public void onMessageCreated(String payload)
      throws JsonProcessingException {

    MessageCreatedEvent event =
        objectMapper.readValue(payload, MessageCreatedEvent.class);

    MessageDto message = messageService.findById(event.messageId());

    String destination =
        "/sub/channels." + message.channelId() + ".messages";

    messagingTemplate.convertAndSend(destination, message);

    log.info(
        "WebSocket 메시지 전달: messageId={}, channelId={}",
        message.id(),
        message.channelId()
    );
  }
}
