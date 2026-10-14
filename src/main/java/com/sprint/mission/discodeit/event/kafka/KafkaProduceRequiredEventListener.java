package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentStorageFailedEvent;
import com.sprint.mission.discodeit.event.message.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.user.RoleUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProduceRequiredEventListener {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(MessageCreatedEvent event) {
    send("discodeit.MessageCreatedEvent", event);
  }

  @Async("eventTaskExecutor")
  @TransactionalEventListener
  public void on(RoleUpdatedEvent event) {
    send("discodeit.RoleUpdatedEvent", event);
  }

  @Async("eventTaskExecutor")
  @EventListener
  public void on(BinaryContentStorageFailedEvent event) {
    send("discodeit.S3UploadFailedEvent", event);
  }

  private void send(String topic, Object event) {
    try {
      String payload = objectMapper.writeValueAsString(event);
      kafkaTemplate.send(topic, payload).whenComplete((result, exception) -> {
        if (exception != null) {
          log.error("Kafka 이벤트 전송 실패: topic={}", topic, exception);
        } else {
          log.debug("Kafka 이벤트 전송 완료: topic={}, partition={}, offset={}",
              topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
        }
      });
    } catch (JsonProcessingException | RuntimeException exception) {
      log.error("Kafka 이벤트 직렬화 또는 전송 요청 실패: topic={}", topic, exception);
    }
  }
}
