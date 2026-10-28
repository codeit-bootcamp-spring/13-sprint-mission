package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.event.UserSseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSseEventListener {

    private final SseService sseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserChanged(UserSseEvent event) {

        sseService.broadcast(
                event.eventName(),
                event.user()
        );

        log.info(
                "사용자 SSE 이벤트 처리 완료. eventName={}, userId={}",
                event.eventName(),
                event.user().id()
        );
    }
}
