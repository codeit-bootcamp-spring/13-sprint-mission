package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.service.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseNotificationTopicListener {

  private final ObjectMapper objectMapper;
  private final SseEmitterManager sseEmitterManager;

  @KafkaListener(
      topics = "discodeit.NotificationCreatedEvent",
      groupId = "${discodeit.kafka.sse-group-id:discodeit-sse-local}"
  )
  public void onNotificationCreated(String payload) throws JsonProcessingException {

    NotificationDto notification = objectMapper.readValue(payload, NotificationDto.class);

    sseEmitterManager.send(notification.receiverId(), notification);

    log.info(
        "SSE 알림 전달: notificationId={}, receiverId={}",
        notification.id(),
        notification.receiverId()
    );
  }
}
