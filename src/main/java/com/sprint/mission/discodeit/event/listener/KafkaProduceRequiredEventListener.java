package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(MessageCreatedEvent event) {
    publish(
        "discodeit.MessageCreatedEvent", event.messageId().toString(), event
    );
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(RoleUpdatedEvent event) {
    publish(
        "discodeit.RoleUpdatedEvent", event.userId().toString(), event
    );
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void handle(S3UploadFailedEvent event) {
    publish(
        "discodeit.S3UploadFailedEvent", event.binaryContentId().toString(), event
    );
  }

  private void publish(String topic, String key, Object event) {
    try {
      String message = objectMapper.writeValueAsString(event);

      kafkaTemplate.send(topic, key, message)
          .whenComplete((result, exception) -> {
            if (exception != null) {
              log.error("Kafka 이벤트 발행 실패: topic={}, key={}", topic, key, exception);
            } else {
              log.info("Kafka 이벤트 발행 완료: topic={}, key={}", topic, key);
            }
          });
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Kafka 이벤트 JSON 변환 실패", e);
    }
  }
}
