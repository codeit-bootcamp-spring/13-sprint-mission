package com.sprint.mission.discodeit.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentUpdateEvent;
import com.sprint.mission.discodeit.event.channel.ChannelEvent;
import com.sprint.mission.discodeit.event.message.MessageCreateEvent;
import com.sprint.mission.discodeit.event.role.RoleUpdateEvent;
import com.sprint.mission.discodeit.event.system.SystemNotifyEvent;
import com.sprint.mission.discodeit.event.user.UserEvent;
import com.sprint.mission.discodeit.mapper.EntityMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.role.Role;
import com.sprint.mission.discodeit.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.sprint.mission.discodeit.config.KafkaConfig.*;

import static com.sprint.mission.discodeit.config.cache.DiscodeItCacheConfig.*;

// todo - 캐시 저장 시, 캐시 키를 UUID 로 변경, 캐시 갱신시, 특정 캐시만 갱신되도록 변경.

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationRequiredTopicListener {
    private final ObjectMapper objectMapper;
    private final ReadStatusRepository readStatusRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    private final EntityMapper entityMapper;

    private final SseService sseService;

    @KafkaListener(topics = MESSAGE_EVENT)
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

            // todo - 모든 서버에 알림이 생성되는 만큼, 알림이 서버갯수만큼 배수로 생김.
            //  로직을 이벤트 리스너가 아닌 이벤트 발생쪽에 생성한다.

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

    @KafkaListener(topics = ROLE_EVENT)
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

    @KafkaListener(topics = SYSTEM_EVENT)
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

    // 유저 정보 변경에 대한 이벤트
    @KafkaListener(topics = USER_EVENT)
    public void userEventConsumer(String event) {
        try {
            log.debug("[EventConsumer] Received event {}", event);
            if (event != null) {
                // 파싱 에러가 안 남.
                UserEvent userEvent = objectMapper.readValue(event, UserEvent.class);

                // user 상태정보 업데이트.
                sseService.broadcast(userEvent.getName(), userEvent.getData());
            }
        } catch (JsonProcessingException e) {
            log.error("[EventConsumer] {} 이벤트 파싱 에러\nevent : {}","UserEvent",event);
            throw new RuntimeException(e);
        }
    }


    // 채널 정보 변경에 대한 이벤트
    @KafkaListener(topics = CHANNEL_EVENT)
    public void channelEventConsumer(String event) {
        try {
            log.debug("[EventConsumer] Received event {}", event);
            if (event != null) {
                ChannelEvent channelEvent = objectMapper.readValue(event, ChannelEvent.class);

                sseService.broadcast(channelEvent.getName(), channelEvent.getData());
            }
        } catch (JsonProcessingException e) {
            log.error("[EventConsumer] {} 이벤트 파싱 에러\nevent : {}","ChannelEvent",event);
            throw new RuntimeException(e);
        }
    }

    // 채널 정보 변경에 대한 이벤트
    @KafkaListener(topics = BINARY_EVENT)
    public void binaryEventConsumer(String event) {
        try {
            log.debug("[EventConsumer] Received event {}", event);
            if (event != null) {
                BinaryContentUpdateEvent binaryEvent = objectMapper.readValue(event, BinaryContentUpdateEvent.class);

                sseService.broadcast(binaryEvent.getName(), binaryEvent.getDto());
            }
        } catch (JsonProcessingException e) {
            log.error("[EventConsumer] {} 이벤트 파싱 에러\nevent : {}","BinaryEvent",event);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = WEBSOCKET_EVENT)
    public void handleMessage(String event) {
        try {
            MessageCreateEvent messageEvent = objectMapper.readValue(event,MessageCreateEvent.class);
            UUID channelId = messageEvent.message().channelId();
            String url = String.format("/sub/channels.%s.messages", channelId);
            messagingTemplate.convertAndSend(url, messageEvent.message());
            log.debug("[EventConsumer] WebSocket 메세지 발송 이벤트 완료. 채널 : {}",channelId);
        } catch (JsonProcessingException e) {
            log.error("[EventConsumer] {} 이벤트 파싱 에러\nevent : {}","UserEvent",event);
            throw new RuntimeException(e);
        }
    }

}


