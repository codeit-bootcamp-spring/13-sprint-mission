package com.sprint.mission.discodeit.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.event.kafka.SseKafkaEvent;
import com.sprint.mission.discodeit.service.SseService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SseEventListener {

    private static final String TOPIC = "discodeit.sse.events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(NotificationCreatedEvent event) {
        publish(List.of(event.receiverId()), "notifications.created", event.notificationDto());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(BinaryContentUpdatedEvent event) {
        publish(null, "binaryContents.updated", event.binaryContentDto());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelCreatedEvent event) {
        publish(event.receiverIds(), "channels.created", event.channelDto());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelUpdatedEvent event) {
        publish(null, "channels.updated", event.channelDto());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ChannelDeletedEvent event) {
        publish(event.receiverIds(), "channels.deleted", event.channelDto());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserCreatedEvent event) {
        publish(null, "users.created", event.userDto());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserUpdatedEvent event) {
        publish(null, "users.updated", event.userDto());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserDeletedEvent event) {
        publish(null, "users.deleted", event.userDto());
    }

    @EventListener
    public void on(UserLogInOutEvent event) {
        UserDto userDto = userService.getUser(event.userId());
        publish(null, "users.updated", userDto);
    }

    private void publish(List<UUID> receiverIds, String eventName, Object data) {
        try {
            SseKafkaEvent event = new SseKafkaEvent(
                    receiverIds,
                    eventName,
                    objectMapper.valueToTree(data)
            );

            String payload = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(TOPIC, payload);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("SSE Kafka 이벤트 직렬화에 실패했습니다.", e);
        }
    }
}
