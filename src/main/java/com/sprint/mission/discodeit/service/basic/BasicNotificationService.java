package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService
        implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @CacheEvict(
            cacheNames = "notificationsByUser",
            key = "#receiverId",
            condition = "#receiverId != null"
    )
    public NotificationDto create(
            UUID receiverId,
            String title,
            String content
    ) {
        if (receiverId == null) {
            throw new IllegalArgumentException(
                    "알림 수신자 ID는 필수입니다."
            );
        }

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() ->
                        new UserNotFoundException(receiverId)
                );

        Notification notification = new Notification(
                receiver,
                title,
                content
        );

        Notification savedNotification =
                notificationRepository.save(notification);

        return notificationMapper.toDto(savedNotification);
    }

    @Override
    @Cacheable(
            cacheNames = "notificationsByUser",
            key = "#receiverId",
            condition = "#receiverId != null"
    )
    public List<NotificationDto> findAllByReceiverId(
            UUID receiverId
    ) {
        if (receiverId == null) {
            throw new IllegalArgumentException(
                    "알림 수신자 ID는 필수입니다."
            );
        }

        return notificationMapper.toDtoList(
                notificationRepository
                        .findAllByReceiverIdOrderByCreatedAtDesc(
                                receiverId
                        )
        );
    }

    @Override
    @Transactional
    @CacheEvict(
            cacheNames = "notificationsByUser",
            key = "#requesterId",
            condition = "#requesterId != null"
    )
    public void delete(
            UUID notificationId,
            UUID requesterId
    ) {
        if (notificationId == null) {
            throw new IllegalArgumentException(
                    "알림 ID는 필수입니다."
            );
        }

        if (requesterId == null) {
            throw new IllegalArgumentException(
                    "요청 사용자 ID는 필수입니다."
            );
        }

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "알림을 찾을 수 없습니다."
                                )
                        );

        if (!notification.getReceiver()
                .getId()
                .equals(requesterId)) {
            throw new AccessDeniedException(
                    "본인의 알림만 확인할 수 있습니다."
            );
        }

        notificationRepository.delete(notification);
    }
}