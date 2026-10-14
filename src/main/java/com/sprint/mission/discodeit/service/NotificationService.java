package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.dto.notification.NotificationDto;
import com.sprint.mission.discodeit.exception.notification.NotificationNotFoundException;
import com.sprint.mission.discodeit.exception.notification.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
  private final NotificationRepository repository;

  @Transactional(readOnly = true)
  @Cacheable(cacheNames = "notificationsByUser", key = "#userId")
  public List<NotificationDto> findAll(UUID userId) {
    return repository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(notification -> new NotificationDto(notification.getId(),
            notification.getCreatedAt(), notification.getUser().getId(),
            notification.getTitle(), notification.getContent()))
        .toList();
  }

  @Transactional
  @CacheEvict(cacheNames = "notificationsByUser", key = "#userId")
  public void read(UUID notificationId, UUID userId) {
    Notification notification = repository.findById(notificationId)
        .orElseThrow(() -> new NotificationNotFoundException(notificationId));
    if (!notification.getUser().getId().equals(userId)) {
      throw new NotificationAccessDeniedException(notificationId);
    }
    repository.delete(notification);
  }
}
