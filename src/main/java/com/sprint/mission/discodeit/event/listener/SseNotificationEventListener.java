package com.sprint.mission.discodeit.event.listener;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import com.sprint.mission.discodeit.service.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseNotificationEventListener {

  private final SseEmitterManager sseEmitterManager;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onNotificationCreated(NotificationCreatedEvent event) {
    NotificationDto notification = event.notification();

    sseEmitterManager.send(notification.receiverId(), notification);

    log.debug("SSE 알림 발송 처리: notificationId={}, receiverId={}", notification.id(),
        notification.receiverId());
  }
}
