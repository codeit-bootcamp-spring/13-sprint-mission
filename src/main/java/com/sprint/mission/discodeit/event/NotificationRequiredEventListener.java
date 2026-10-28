
package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationRequiredEventListener {

  private final ReadStatusRepository readStatusRepository;
  private final NotificationService notificationService;

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleMessageCreated(MessageCreatedEvent event) {
    String title = event.authorName() + "님의 새 메시지 (" + event.channelName() + ")";
    String content = event.content() != null && !event.content().isBlank()
        ? event.content()
        : "새 메시지가 도착했습니다.";

    for (ReadStatus readStatus : readStatusRepository.findByChannelId(event.channelId())) {
      if (!readStatus.isNotificationEnabled()) {
        continue;
      }

      if (readStatus.getUser().getId().equals(event.authorId())) {
        continue;
      }

      notificationService.create(readStatus.getUser().getId(), title, content);
    }
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleRoleUpdated(RoleUpdatedEvent event) {
    String title = "사용자 권한 변경";
    String content = "권한이 " + event.previousRole()
        + "에서 " + event.newRole() + "로 변경되었습니다.";

    notificationService.create(event.userId(), title, content);
  }
}
