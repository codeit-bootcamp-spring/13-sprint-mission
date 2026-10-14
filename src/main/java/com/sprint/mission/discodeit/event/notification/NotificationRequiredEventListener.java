package com.sprint.mission.discodeit.event.notification;

import com.sprint.mission.discodeit.entity.Notification;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.binarycontent.BinaryContentStorageFailedEvent;
import com.sprint.mission.discodeit.event.message.MessageCreatedEvent;
import com.sprint.mission.discodeit.event.user.RoleUpdatedEvent;
import com.sprint.mission.discodeit.repository.NotificationRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

// Kafka 알림 리스너를 사용하므로 @Component를 제외해 빈 등록을 비활성화한다.
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final NotificationRepository notificationRepository;
  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;

  @EventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @CacheEvict(cacheNames = "notificationsByUser", allEntries = true)
  public void on(BinaryContentStorageFailedEvent event) {
    String content = """
        Operation: %s
        RequestId: %s
        BinaryContentId: %s
        Error: %s
        """.formatted(event.operation(), event.requestId(), event.binaryContentId(), event.reason());
    var notifications = userRepository.findAllByRole(Role.ADMIN).stream()
        .map(admin -> new Notification(admin, "S3 바이너리 데이터 저장에 실패했습니다.", content))
        .toList();
    notificationRepository.saveAll(notifications);
  }

  // AFTER_COMMIT에서 실행하므로 알림 저장에는 별도의 트랜잭션이 필요하다.
  @Async
  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @CacheEvict(cacheNames = "notificationsByUser", allEntries = true)
  public void on(MessageCreatedEvent event) {
    var notifications = readStatusRepository
        .findAllByChannelIdAndNotificationEnabledTrue(event.channelId()).stream()
        .filter(status -> !status.getUser().getId().equals(event.authorId()))
        .map(status -> {
          String channelName = status.getChannel().getName();
          if (channelName == null) {
            channelName = "비공개 채널";
          }
          return new Notification(status.getUser(),
              event.authorName() + " (#" + channelName + ")",
              event.content() == null ? "첨부파일 메시지" : event.content());
        })
        .toList();
    notificationRepository.saveAll(notifications);
  }

  @Async
  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @CacheEvict(cacheNames = "notificationsByUser", allEntries = true)
  public void on(RoleUpdatedEvent event) {
    userRepository.findById(event.userId()).ifPresent(user -> notificationRepository.save(
        new Notification(user, "권한이 변경되었습니다.",
            event.previousRole() + " -> " + event.newRole())));
  }
}
