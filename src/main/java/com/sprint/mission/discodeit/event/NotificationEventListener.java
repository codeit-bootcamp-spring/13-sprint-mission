package com.sprint.mission.discodeit.event;


import com.sprint.mission.discodeit.config.CacheConfig;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationRepository notificationRepository;
    private final ReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;

    @Async("noty")
    @CacheEvict(cacheNames = CacheConfig.NOTIFICATION_CACHE, allEntries = true)
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(RoleUpdateEvent event){
        log.debug("[Event] - RoleUpdateEvent - {}", event.id());
        notificationRepository.save(new Notification(
                event.id(),
                "권한 변경 알림",
                "권한이 변경되었습니다. - " + event.role().toString()
        ));
    }


    /**
     * 시스템 작업 오류 메세지를 관리자 원한을 가진 사용자에게 알림 발송.
     * @param event - 1. 작업 내용, 2. 리퀘스트 아이디, 3. 발생한 에러 메세지
     */
    @Async("noty")
    @CacheEvict(cacheNames = CacheConfig.NOTIFICATION_CACHE, allEntries = true)
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(SystemNotifyEvent event){
        log.debug("[SystemEvent] - 시스템 알림 이벤트 생성.");
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        admins.forEach(
                user -> notificationRepository.save(new Notification(
                        user.getId(),
                        event.job() + "작업 실패",
                        message(event.job(), event.requestId(), event.exceptionMessage())
                ))

        );
    }

    private String message(String job, String requestId, String exceptionMessage){
        StringBuilder message = new StringBuilder();
        message.append("작업 이름: ").append(job);
        message.append("요청 ID: ").append(requestId);
        message.append("에러 메세지: ").append(exceptionMessage);
        return message.toString();
    }


    /**
     * 메세지를 생성할 때, 해당 채널을 구독한 구독자에게 알림을 보냄.
     * 단, 메세지를 생성한 사용자는 제외.
     * @param event 메세지 생성 이벤트
     */
    @Async("noty")
    @CacheEvict(cacheNames = CacheConfig.NOTIFICATION_CACHE, allEntries = true)
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(MessageCreateEvent event){
        log.debug("[MessageEvent] - 메세지 생성 이벤트 시작. id : {}", event.id());

        List<ReadStatus> readStatuses = readStatusRepository.findAllByChannelIdWithStatusOn(event.channelId(),true);

        List<Notification> notifications = readStatuses.stream()
                .map(readStatus -> getNotificationFromUser(event,readStatus.getUser()))
                .filter(Objects::nonNull)
                .toList();

        log.debug("[MessageEvent] - 알림 대상 : {} 건", notifications.size());

        notificationRepository.saveAll(notifications);

        log.debug("[MessageEvent] - 메세지 생성 알림 생성 완료. - id : {}", event.id());
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

}
