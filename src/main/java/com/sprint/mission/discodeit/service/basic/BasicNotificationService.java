package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicNotificationService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @CacheEvict(cacheNames = "notifications", key = "#receiverId")
    public NotificationDto create(
            UUID receiverId,
            String title,
            String content
    ) {
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(
                        () -> new UserNotFoundException(receiverId)
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
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "notifications", key = "#receiverId")
    public List<NotificationDto> findAllByReceiverId(
            UUID receiverId
    ) {
        return notificationRepository
                .findAllByReceiverIdOrderByCreatedAtDesc(receiverId)
                .stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Override
    @CacheEvict(cacheNames = "notifications", key = "#requesterId")
    public void delete(
            UUID notificationId,
            UUID requesterId
    ) {
        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(
                                () -> new NotificationNotFoundException(
                                        notificationId
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