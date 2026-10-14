package com.sprint.mission.discodeit.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.message.MessageCreateEvent;
import com.sprint.mission.discodeit.event.role.RoleUpdateEvent;
import com.sprint.mission.discodeit.event.system.SystemNotifyEvent;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.role.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {
    private final ObjectMapper objectMapper;
    private final ReadStatusRepository readStatusRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @KafkaListener(topics = "discodeit.MessageCreatedEvent")
    public void onMessageCreatedEvent(String kafkaEvent) {
        try {
            // 카프카에서의 메세지를 읽고, 원래의 이벤트로 변환.
            MessageCreateEvent event = objectMapper.readValue(kafkaEvent, MessageCreateEvent.class);
            log.debug("[MessageEvent] - 메세지 생성 이벤트 시작. id : {}", event.id());

            List<ReadStatus> readStatuses = readStatusRepository.findAllByChannelIdWithStatusOn(event.channelId(),true);

            List<Notification> notifications = readStatuses.stream()
                    .map(readStatus -> getNotificationFromUser(event,readStatus.getUser()))
                    .filter(Objects::nonNull)
                    .toList();

            log.debug("[MessageEvent] - 알림 대상 : {} 건", notifications.size());

            notificationRepository.saveAll(notifications);

            log.debug("[MessageEvent] - 메세지 생성 알림 생성 완료. - id : {}", event.id());

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private Notification getNotificationFromUser(MessageCreateEvent event, User user){
        log.debug("[MessageEvent] - 아이디 유저 체크 - 작성자 : {}, 알림 대상 : {}", event.userId(), user.getId());
        if (user.getId().equals(event.userId()))
            return null;
        else
            return new Notification(
                    user.getId(),
                    user.getUsername(),
                    event.content()
            );
    }

    @KafkaListener(topics = "discodeit.RoleUpdateEvent")
    public void onRoleUpdatedEvent(String kafkaEvent) {
        try {
            RoleUpdateEvent event = objectMapper.readValue(kafkaEvent,RoleUpdateEvent.class);

            log.debug("[Event] - RoleUpdateEvent - {}", event.id());
            notificationRepository.save(new Notification(
                    event.id(),
                    "권한 변경 알림",
                    "권한이 변경되었습니다. - " + event.role().toString()
            ));

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.SystemNotifyEvent")
    public void onS3UploadFailedEvent(String kafkaEvent) {
        try {
            SystemNotifyEvent event = objectMapper.readValue(kafkaEvent,SystemNotifyEvent.class);

            log.debug("[SystemEvent] - 시스템 알림 이벤트 생성.");
            List<User> admins = userRepository.findByRole(Role.ADMIN);

            admins.forEach(
                    user -> notificationRepository.save(new Notification(
                            user.getId(),
                            event.job() + "작업 실패",
                            message(event.job(), event.requestId(), event.exceptionMessage())
                    ))

            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String message(String job, String requestId, String exceptionMessage){
        StringBuilder message = new StringBuilder();
        message.append("작업 이름: ").append(job);
        message.append("요청 ID: ").append(requestId);
        message.append("에러 메세지: ").append(exceptionMessage);
        return message.toString();
    }
}


