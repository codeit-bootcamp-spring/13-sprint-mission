package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.aspect.LogAction;
import com.sprint.mission.discodeit.config.CacheConfig;
import com.sprint.mission.discodeit.dto.command.notification.NotificationCreateCommand;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.events.NotificationCacheClearEvent;
import com.sprint.mission.discodeit.events.NotificationCreatedEvent;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class BasicNotificationService implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserReader userReader;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void createAll(NotificationCreateCommand command) {
        List<Notification> createdNotifications = command.receiverIds()
                .stream()
                .filter(Objects::nonNull)
                .map(receiverId -> Notification.of(command, userReader.getReferenceUser(receiverId)))
                .toList();

        if (createdNotifications.isEmpty()) {
            log.debug("알림 수신자가 없어 저장을 생략합니다.");
            return;
        }

        notificationRepository.saveAll(createdNotifications);
        log.debug("알림 저장 요청을 처리했습니다. notificationCount={}", createdNotifications.size());

        createdNotifications
                .forEach(noti -> {
                    eventPublisher.publishEvent(new NotificationCacheClearEvent(noti.getReceiverId()));

                    // 저장한 알림마다 DTO를 발행한다. SSE 리스너는 커밋 후 해당 알림의 수신자에게만 보낸다.
                    eventPublisher.publishEvent(new NotificationCreatedEvent(notificationMapper.toDto(noti)));
                });
    }

    @Override
    @LogAction(value = "알림 확인 및 삭제", idName = "notificationId", idParamIndex = 0)
    @PreAuthorize("@notificationGuard.isOwner(#notificationId, authentication.principal.userDto.id)")
    public void delete(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        notificationRepository.delete(notification);
        log.debug("알림 삭제 요청을 처리했습니다. notificationId={}, receiverId={}",
                notificationId, notification.getReceiverId());

        eventPublisher.publishEvent(new NotificationCacheClearEvent(notification.getReceiverId()));
    }


    @Cacheable(cacheNames = CacheConfig.USERS_BY_NOTIFICATIONS,
            keyGenerator = "currentUserIdKeyGenerator"
    )
    @Transactional(readOnly = true)
    @Override
    public List<NotificationDto> findAllForCurrentUser() {
        DiscodeitUserDetails userDetails = (DiscodeitUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        UUID userId = userDetails.getUserDto().id();

        List<NotificationDto> notifications = notificationRepository.findAllByReceiverId(userId)
                .stream()
                .map(notificationMapper::toDto)
                .toList();
        log.debug("사용자 알림 목록을 조회했습니다. userId={}, notificationCount={}", userId, notifications.size());
        return notifications;
    }
}
