package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final NotificationRequiredEventListener notificationListener;

  @Transactional
  @KafkaListener(
      topics = "discodeit.MessageCreatedEvent",
      groupId = "discodeit-group"
  )
  public void consumeMessageCreated(String payload) throws JsonProcessingException {

    MessageCreatedEvent event = objectMapper.readValue(payload, MessageCreatedEvent.class);

    notificationListener.handle(event);

    log.info("Kafka 메시지 생성 이벤트 처리완료: messageId={}", event.messageId());
  }

  @Transactional
  @KafkaListener(
      topics = "discodeit.RoleUpdatedEvent",
      groupId = "discodeit-group"
  )
  public void consumeRoleUpdated(String payload) throws JsonProcessingException {

    RoleUpdatedEvent event = objectMapper.readValue(payload, RoleUpdatedEvent.class);

    notificationListener.handle(event);

    log.info("Kafka 권한 변경 이벤트 처리 완료: userId={}", event.userId());
  }

  @Transactional
  @KafkaListener(
      topics = "discodeit.S3UploadFailedEvent",
      groupId = "discodeit-group"
  )
  public void consumeS3UploadFailed(String payload) throws JsonProcessingException {

    S3UploadFailedEvent event = objectMapper.readValue(payload, S3UploadFailedEvent.class);

    notificationListener.handle(event);

    log.info(
        "Kafka S3 업로드 실패 이벤트 처리 완료: binaryContentId={}",
        event.binaryContentId()
    );
  }
}
