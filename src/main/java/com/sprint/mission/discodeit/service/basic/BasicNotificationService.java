package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.NotificationDto;
import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.exception.NotificationAccessDeniedException;
import com.sprint.mission.discodeit.exception.NotificationNotFoundException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @CacheEvict(cacheNames = "notificationsByUser", key = "#receiverId")
  public NotificationDto create(UUID receiverId, String title, String content) {
    User receiver = userRepository.findById(receiverId)
        .orElseThrow(() -> new UserNotFoundException(
            Map.of("userId", receiverId)
        ));

    Notification notification = new Notification(receiver, title, content);
    notificationRepository.save(notification);

    NotificationDto notificationDto = toDto(notification);

    eventPublisher.publishEvent(
        new NotificationCreatedEvent(notificationDto)
    );

    return notificationDto;
  }

  @Override
  @Cacheable(cacheNames = "notificationsByUser", key = "#receiverId")
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId)
        .stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = "notificationsByUser", key = "#requesterId")
  public void delete(UUID notificationId, UUID requesterId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NotificationNotFoundException(
            Map.of("notificationId", notificationId)
        ));

    if (!notification.getReceiver().getId().equals(requesterId)) {
      throw new NotificationAccessDeniedException(
          Map.of("notificationId", notificationId)
      );
    }

    notificationRepository.delete(notification);
  }

  private NotificationDto toDto(Notification notification) {
    return new NotificationDto(
        notification.getId(),
        notification.getCreatedAt(),
        notification.getReceiver().getId(),
        notification.getTitle(),
        notification.getContent()
    );
  }
}