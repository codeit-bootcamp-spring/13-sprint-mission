package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.CacheNames;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationRequiredTopicListener {

    private final ObjectMapper objectMapper;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    @KafkaListener(topics = "discodeit.MessageCreatedEvent")
    @CacheEvict(
            value = CacheNames.USER_NOTIFICATIONS,
            allEntries = true
    )
    public void onMessageCreatedEvent(String kafkaEvent) {
        try {
            MessageCreatedEvent event = objectMapper.readValue(kafkaEvent, MessageCreatedEvent.class);

            Message message = messageRepository.findById(event.messageId())
                    .orElseThrow(() -> new MessageNotFoundException(event.messageId()));

            UUID authorId = message.getAuthor().getId();

            List<Notification> notifications = readStatusRepository
                    .findAllByChannelIdAndNotificationEnabledTrue(message.getChannel().getId())
                    .stream()
                    .filter(readStatus -> !readStatus.getUser().getId().equals(authorId))
                    .map(readStatus -> new Notification(
                            readStatus.getUser(),
                            message.getAuthor().getUsername()
                                    + " (#"
                                    + message.getChannel().getName()
                                    + ")",
                            message.getContent()
                    ))
                    .toList();

            notificationRepository.saveAll(notifications);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("MessageCreatedEvent 역직렬화에 실패했습니다.", e);
        }
    }

    @Transactional
    @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
    @CacheEvict(
            value = CacheNames.USER_NOTIFICATIONS,
            allEntries = true
    )
    public void onRoleUpdatedEvent(String kafkaEvent) {
        try {
            RoleUpdatedEvent event = objectMapper.readValue(kafkaEvent, RoleUpdatedEvent.class);

            User receiver = userRepository.findById(event.userId())
                    .orElseThrow(() -> new UserNotFoundException(event.userId()));

            Notification notification = new Notification(
                    receiver,
                    "권한이 변경되었습니다.",
                    event.oldRole() + " -> " + event.newRole()
            );

            notificationRepository.save(notification);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("RoleUpdatedEvent 역직렬화에 실패했습니다.", e);
        }
    }

    @Transactional
    @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
    @CacheEvict(
            value = CacheNames.USER_NOTIFICATIONS,
            allEntries = true
    )
    public void onS3UploadFailedEvent(String kafkaEvent) {
        try {
            S3UploadFailedEvent event = objectMapper.readValue(kafkaEvent, S3UploadFailedEvent.class);

            String content = """
                    Task: %s
                    RequestId: %s
                    BinaryContentId: %s
                    Error: %s
                    """.formatted(
                    event.taskName(),
                    event.requestId(),
                    event.binaryContentId(),
                    event.errorMessage()
            );

            List<User> admins = userRepository.findAllByRole(Role.ADMIN);

            List<Notification> notifications = admins.stream()
                    .map(admin -> new Notification(
                            admin,
                            "바이너리 데이터 저장 실패",
                            content
                    ))
                    .toList();

            notificationRepository.saveAll(notifications);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("S3UploadFailedEvent 역직렬화에 실패했습니다.", e);
        }
    }
}
