package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentStorageFailedEvent;
import com.sprint.mission.discodeit.event.message.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.user.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredTopicListener {

  private final ObjectMapper objectMapper;
  private final NotificationRepository notificationRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;

  @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
  @Transactional
  @CacheEvict(cacheNames = "notificationsByUser", allEntries = true)
  public void onS3UploadFailedEvent(String kafkaEvent) {
    BinaryContentStorageFailedEvent event = readEvent(kafkaEvent, BinaryContentStorageFailedEvent.class);
    String content = """
        Operation: %s
        RequestId: %s
        BinaryContentId: %s
        Error: %s
        """.formatted(event.operation(), event.requestId(), event.binaryContentId(), event.reason());
    var notifications = userRepository.findAllByRole(Role.ADMIN).stream()
        .map(admin -> new Notification(admin, "S3 바이너리 데이터 저장에 실패했습니다.", content))
        .toList();
    notificationRepository.saveAll(notifications);
  }

  @KafkaListener(topics = "discodeit.MessageCreatedEvent")
  @Transactional
  @CacheEvict(cacheNames = "notificationsByUser", allEntries = true)
  public void onMessageCreatedEvent(String kafkaEvent) {
    MessageCreatedEvent event = readEvent(kafkaEvent, MessageCreatedEvent.class);
    var notifications = readStatusRepository
        .findAllByChannelIdAndNotificationEnabledTrue(event.channelId()).stream()
        .filter(status -> !status.getUser().getId().equals(event.authorId()))
        .map(status -> {
          String channelName = status.getChannel().getName();
          if (channelName == null) {
            channelName = "비공개 채널";
          }
          return new Notification(status.getUser(),
              event.authorName() + " (#" + channelName + ")",
              event.content() == null ? "첨부파일 메시지" : event.content());
        })
        .toList();
    notificationRepository.saveAll(notifications);
  }

  @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
  @Transactional
  @CacheEvict(cacheNames = "notificationsByUser", allEntries = true)
  public void onRoleUpdatedEvent(String kafkaEvent) {
    RoleUpdatedEvent event = readEvent(kafkaEvent, RoleUpdatedEvent.class);
    userRepository.findById(event.userId()).ifPresent(user -> notificationRepository.save(
        new Notification(user, "권한이 변경되었습니다.",
            event.previousRole() + " -> " + event.newRole())));
  }

  private <T> T readEvent(String kafkaEvent, Class<T> eventType) {
    try {
      return objectMapper.readValue(kafkaEvent, eventType);
    } catch (JsonProcessingException exception) {
      log.error("Kafka 이벤트 역직렬화 실패: eventType={}", eventType.getSimpleName(), exception);
      throw new IllegalArgumentException("Kafka 이벤트 역직렬화 실패: " + eventType.getSimpleName(), exception);
    }
  }
}
