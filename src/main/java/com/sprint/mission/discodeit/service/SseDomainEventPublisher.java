package com.sprint.mission.discodeit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.SseEvent;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseDomainEventPublisher {

  private static final String TOPIC = "discodeit.SseDomainEvent";

  private final ApplicationEventPublisher eventPublisher;
  private final RedisSseEventStore redisSseEventStore;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;
  private final UserRepository userRepository;

  public void publishToUser(
      UUID receiverId,
      String eventName,
      Object data
  ) {
    SseEvent event = new SseEvent(
        UUID.randomUUID(),
        receiverId,
        eventName,
        objectMapper.valueToTree(data)
    );

    eventPublisher.publishEvent(event);
  }

  public void publishToAllUsers(
      String eventName,
      Object data
  ) {
    userRepository.findAll().stream()
        .map(User::getId)
        .distinct()
        .forEach(receiverId ->
            publishToUser(
                receiverId,
                eventName,
                data
            )
        );
  }

  @TransactionalEventListener(
      phase = TransactionPhase.AFTER_COMMIT,
      fallbackExecution = true
  )
  public void onEvent(SseEvent event) {

    try {
      redisSseEventStore.save(event);
    } catch (RuntimeException e) {
      log.error(
          "SSE 도메인 이벤트 Redis 저장 실패: eventId={}",
          event.id(),
          e
      );
    }

    try {
      String payload = objectMapper.writeValueAsString(event);

      kafkaTemplate.send(
          TOPIC,
          event.receiverId().toString(),
          payload
      ).whenComplete((result, exception) -> {
        if (exception != null) {
          log.error(
              "SSE 도메인 이벤트 Kafka 발행 실패: eventId={}",
              event.id(),
              exception
          );
        } else {
          log.info(
              "SSE 도메인 이벤트 Kafka 발행 성공: eventId={}, eventName={}",
              event.id(),
              event.name()
          );
        }
      });

    } catch (JsonProcessingException e) {
      log.error(
          "SSE 도메인 이벤트 직렬화 실패: eventId={}",
          event.id(),
          e
      );
    }
  }
}
