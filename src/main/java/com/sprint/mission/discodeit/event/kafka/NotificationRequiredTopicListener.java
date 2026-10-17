package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRequiredTopicListener {

    private final ObjectMapper objectMapper;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @KafkaListener(topics = "discodeit.MessageCreatedEvent")
    @Transactional
    public void onMessageCreatedEvent(String kafkaEvent) {
        try {
            MessageCreatedEvent event =
                    objectMapper.readValue(
                            kafkaEvent,
                            MessageCreatedEvent.class
                    );

            Message message = messageRepository
                    .findById(event.messageId())
                    .orElseThrow(() ->
                            new MessageNotFoundException(
                                    event.messageId()
                            )
                    );

            List<ReadStatus> receivers =
                    readStatusRepository
                            .findAllByChannelIdAndNotificationEnabledTrue(
                                    message.getChannel().getId()
                            );

            String title =
                    message.getAuthor().getUsername()
                            + " (#"
                            + message.getChannel().getName()
                            + ")";

            receivers.stream()
                    .filter(readStatus ->
                            !readStatus.getUser().getId()
                                    .equals(message.getAuthor().getId())
                    )
                    .forEach(readStatus ->
                            notificationService.create(
                                    readStatus.getUser().getId(),
                                    title,
                                    message.getContent()
                            )
                    );

            log.info(
                    "메시지 생성 Kafka 이벤트 처리 완료. messageId={}",
                    event.messageId()
            );

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "MessageCreatedEvent 역직렬화 실패",
                    e
            );
        }
    }

    @KafkaListener(topics = "discodeit.RoleUpdatedEvent")
    public void onRoleUpdatedEvent(String kafkaEvent) {
        try {
            RoleUpdatedEvent event =
                    objectMapper.readValue(
                            kafkaEvent,
                            RoleUpdatedEvent.class
                    );

            String title = "권한이 변경되었습니다.";
            String content =
                    event.previousRole().name()
                            + " -> "
                            + event.newRole().name();

            notificationService.create(
                    event.userId(),
                    title,
                    content
            );

            log.info(
                    "권한 변경 Kafka 이벤트 처리 완료. userId={}",
                    event.userId()
            );

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "RoleUpdatedEvent 역직렬화 실패",
                    e
            );
        }
    }

    @KafkaListener(topics = "discodeit.S3UploadFailedEvent")
    public void onS3UploadFailedEvent(String kafkaEvent) {
        try {
            S3UploadFailedEvent event =
                    objectMapper.readValue(
                            kafkaEvent,
                            S3UploadFailedEvent.class
                    );

            List<User> admins =
                    userRepository.findAllByRole(Role.ADMIN);

            String title = "S3 파일 업로드 실패";
            String content =
                    "Task: " + event.taskName()
                            + ", RequestId: " + event.requestId()
                            + ", BinaryContentId: "
                            + event.binaryContentId()
                            + ", Error: " + event.errorMessage();

            admins.forEach(admin ->
                    notificationService.create(
                            admin.getId(),
                            title,
                            content
                    )
            );

            log.info(
                    "S3 업로드 실패 Kafka 이벤트 처리 완료. binaryContentId={}",
                    event.binaryContentId()
            );

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "S3UploadFailedEvent 역직렬화 실패",
                    e
            );
        }
    }
}