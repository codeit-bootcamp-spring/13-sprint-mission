package com.sprint.mission.discodeit.event.kafka;

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

    @Async("taskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void on(MessageCreatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.send("discodeit.MessageCreatedEvent", payload);

            log.info("Kafka MessageCreatedEvent published. payload={}", payload);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("MessageCreatedEvent 직렬화에 실패했습니다.", e);
        }
    }

    @Async("taskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void on(RoleUpdatedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.send("discodeit.RoleUpdatedEvent", payload);

            log.info("Kafka RoleUpdatedEvent published. payload={}", payload);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("RoleUpdatedEvent 직렬화에 실패했습니다.", e);
        }
    }

    @Async("taskExecutor")
    @EventListener
    public void on(S3UploadFailedEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.send("discodeit.S3UploadFailedEvent", payload);

            log.info("Kafka S3UploadFailedEvent published. payload={}", payload);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("S3UploadFailedEvent 직렬화에 실패했습니다.", e);
        }
    }
}
