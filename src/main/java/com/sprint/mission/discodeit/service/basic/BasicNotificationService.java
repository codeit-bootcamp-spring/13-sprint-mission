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
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final NotificationMapper notificationMapper;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @Override
  @CacheEvict(cacheNames = "notifications", key = "#receiverId")
  public NotificationDto create(UUID receiverId, String title, String content) {
    log.debug("알림 생성 시작: receiverId={}", receiverId);

    User receiver = userRepository.findById(receiverId)
        .orElseThrow(() -> new UserNotFoundException(receiverId));

    Notification notification = new Notification(receiver, title, content);

    notificationRepository.save(notification);

    log.info("알림 생성 완료: notificationId={}, receiverId={}", notification.getId(), receiverId);

    return notificationMapper.toDto(notification);
  }

  @Transactional(readOnly = true)
  @Override
  @Cacheable(cacheNames = "notifications", key = "#receiverId")
  public List<NotificationDto> findAllByReceiverId(UUID receiverId) {
    log.debug("사용자별 알림 조회 시작: receiverId={}", receiverId);

    List<NotificationDto> notifications = notificationRepository
        .findByReceiverIdOrderByCreatedAtDesc(receiverId)
        .stream()
        .map(notificationMapper::toDto)
        .toList();

    log.debug("사용자별 알림 조회 완료: receiverId={}, count={}", receiverId, notifications.size());

    return notifications;
  }

  @Override
  @CacheEvict(cacheNames = "notifications", key = "#requesterId")
  public void delete(UUID notificationId, UUID requesterId) {
    log.debug("알림 삭제 시작: notificationId={}, requesterId={}", notificationId, requesterId);

    Notification notification = notificationRepository
        .findById(notificationId)
        .orElseThrow(() -> new NotificationNotFoundException(notificationId));

    if (!notification.getReceiver().getId().equals(requesterId)) {
      throw new AccessDeniedException("다른 사용자의 알림은 삭제할 수 없습니다.");
    }

    notificationRepository.delete(notification);

    log.info("알림 삭제 완료: notificationId={}, requesterId={}", notificationId, requesterId);

  }
}
