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

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProduceRequiredEventListener {

    private static final String MESSAGE_CREATED_TOPIC =
            "discodeit.MessageCreatedEvent";

    private static final String ROLE_UPDATED_TOPIC =
            "discodeit.RoleUpdatedEvent";

    private static final String S3_UPLOAD_FAILED_TOPIC =
            "discodeit.S3UploadFailedEvent";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async("eventTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void on(MessageCreatedEvent event) {
        send(MESSAGE_CREATED_TOPIC, event);
    }

    @Async("eventTaskExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void on(RoleUpdatedEvent event) {
        send(ROLE_UPDATED_TOPIC, event);
    }

    @Async("eventTaskExecutor")
    @EventListener
    public void on(S3UploadFailedEvent event) {
        send(S3_UPLOAD_FAILED_TOPIC, event);
    }

    private void send(String topic, Object event) {
        try {
            String payload =
                    objectMapper.writeValueAsString(event);

            kafkaTemplate.send(topic, payload)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.error(
                                    "Kafka 이벤트 발행 실패. topic={}, payload={}",
                                    topic,
                                    payload,
                                    exception
                            );
                            return;
                        }

                        log.info(
                                "Kafka 이벤트 발행 완료. topic={}, partition={}, offset={}",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset()
                        );
                    });

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Kafka 이벤트 직렬화에 실패했습니다. topic=" + topic,
                    e
            );
        }
    }
}
