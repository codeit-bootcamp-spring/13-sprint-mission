package com.sprint.mission.discodeit.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.cache.CacheConfig;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.message.MessageCreateEvent;
import com.sprint.mission.discodeit.event.role.RoleUpdateEvent;
import com.sprint.mission.discodeit.event.system.SystemNotifyEvent;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;


// todo - 캐시 저장 시, 캐시 키를 UUID 로 변경, 캐시 갱신시, 특정 캐시만 갱신되도록 변경.

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {
    private final ObjectMapper objectMapper;
    private final ReadStatusRepository readStatusRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private final EntityMapper entityMapper;

    private final SseService sseService;

    @KafkaListener(topics = "discodeit.event.message")
    @CacheEvict(cacheNames = CacheConfig.NOTIFICATION_CACHE, allEntries = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMessageCreatedEvent(String kafkaEvent) {
        log.info("Received message created event: {}", kafkaEvent);
        try {
            // 카프카에서의 메세지를 읽고, 원래의 이벤트로 변환.
            MessageCreateEvent event = objectMapper.readValue(kafkaEvent, MessageCreateEvent.class);
            log.debug("[MessageEvent] - 메세지 생성 이벤트 시작. 메세지 id : {}", event.message().id());

            // 메세지 생성 이후,
            List<ReadStatus> readStatuses = readStatusRepository.findAllByChannelIdWithStatusOn(event.message().channelId(), true);

            List<Notification> notifications = readStatuses.stream()
                    .map(readStatus -> getNotificationFromUser(event,readStatus.getUser()))
                    .filter(Objects::nonNull)
                    .toList();

            log.debug("[MessageEvent] - 알림 대상 : {} 건", notifications.size());

            notificationRepository.saveAll(notifications);

            notifications.forEach(notification ->
                    sseService.send(
                            List.of(notification.getReceiverId()),
                            "notifications.created",
                            entityMapper.toDto(notification)
                    ));

        } catch (JsonProcessingException e) {
            log.warn("[{}] - 메세지 생성 알림 발송 실패","MessageEvent");
            throw new RuntimeException(e);
        }
    }
    private Notification getNotificationFromUser(MessageCreateEvent event, User user){
        if (user.getId().equals(event.message().author().id()))
            // 작성자 == 유저 라면 알림 생략
            return null;
        else{
            log.debug("[MessageEvent] - 아이디 유저 체크 - 작성자 : {}, 알림 대상 : {}", event.message().author().id(),user.getId());
            return new Notification(
                    user.getId(),   // 이 친구가 대상자 인데, 트랜젝션에 의해 값이 사라진다?
                    user.getUsername(),
                    event.message().content()
            );
        }
    }

    @KafkaListener(topics = "discodeit.event.role")
    @CacheEvict(cacheNames = CacheConfig.NOTIFICATION_CACHE, allEntries = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onRoleUpdatedEvent(String kafkaEvent) {
        try {
            RoleUpdateEvent event = objectMapper.readValue(kafkaEvent,RoleUpdateEvent.class);

            log.debug("[Event] - RoleUpdateEvent - {}", event.userId());
            Notification notification = new Notification(
                    event.userId(),
                    "권한 변경 알림",
                    String.format("권한이 변경되었습니다. %s -> %s", event.before().toString(), event.after().toString())
            );

            sseService.send(
                    List.of(notification.getReceiverId()),
                    "notifications.created",
                    entityMapper.toDto(notification)
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "discodeit.event.system")
    @CacheEvict(cacheNames = CacheConfig.NOTIFICATION_CACHE, allEntries = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onS3UploadFailedEvent(String kafkaEvent) {
        try {
            SystemNotifyEvent event = objectMapper.readValue(kafkaEvent,SystemNotifyEvent.class);

            log.debug("[SystemEvent] - 시스템 알림 이벤트 생성.");
            List<User> admins = userRepository.findByRole(Role.ADMIN);

            admins.forEach(
                    user -> {
                        Notification notification = new Notification(
                            user.getId(),
                            event.job() + "작업 실패",
                            message(event.job(), event.requestId(), event.exceptionMessage())
                        );
                        sseService.send(
                                List.of(notification.getReceiverId()),
                                "notifications.created",
                                entityMapper.toDto(notification)
                        );
                    }

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


