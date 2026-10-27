package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.event.SseEvent;
import com.sprint.mission.discodeit.service.RedisSseEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseNotificationEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final RedisSseEventStore redisSseEventStore;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onNotificationCreated(NotificationCreatedEvent event) {
    NotificationDto notification = event.notification();

    SseEvent sseEvent = new SseEvent(
        notification.id(),
        notification.receiverId(),
        "notifications.created",
        objectMapper.valueToTree(notification)
    );

    try {
      redisSseEventStore.save(sseEvent);
    } catch (RuntimeException e) {
      log.error(
          "SSE 알림 Redis 저장 실패: notificationId={}",
          notification.id(),
          e
      );
    }

    try {
      String payload = objectMapper.writeValueAsString(notification);

      kafkaTemplate.send(
          "discodeit.NotificationCreatedEvent",
          notification.receiverId().toString(),
          payload
      ).whenComplete((result, exception) -> {
        if (exception != null) {
          log.error(
              "SSE 알림 Kafka 발행 실패: notificationId={}",
              notification.id(),
              exception
          );
        } else {
          log.info(
              "SSE 알림 Kafka 발행 성공: notificationId={}",
              notification.id()
          );
        }
      });

    } catch (JsonProcessingException e) {
      throw new IllegalStateException("SSE 알림 JSON 변환 실패", e);
    }
  }
}
