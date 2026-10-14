package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.CacheNames;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.exception.notification.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.mapper.NotificationMapper;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicNotificationService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Cacheable(
            value = CacheNames.USER_NOTIFICATIONS,
            key = "#userId"
    )
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(UUID userId) {
        return notificationRepository.findAllByReceiverId(userId)
                .stream()
                .map(notificationMapper::toDto)
                .toList();
    }

    @Override
    @CacheEvict(
            value = CacheNames.USER_NOTIFICATIONS,
            key = "#userId"
    )
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        // 알림 검색
        Notification notificationTemp = notificationRepository.findById(notificationId)
                .orElseThrow(NotificationNotFoundException::new);

        if (!notificationTemp.getReceiver().getId().equals(userId)) {
            throw new NotificationAccessDeniedException();
        }

        notificationRepository.deleteById(notificationId);
    }
}
