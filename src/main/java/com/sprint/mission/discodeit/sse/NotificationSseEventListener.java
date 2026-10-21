package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.event.NotificationCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSseEventListener {

    private final SseService sseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationCreated(
            NotificationCreatedEvent event
    ) {

        sseService.send(
                event.receiverId(),
                "notifications.created",
                event.notification()
        );

        log.info(
                "알림 SSE 이벤트 전송. receiverId={}",
                event.receiverId()
        );
    }

}
