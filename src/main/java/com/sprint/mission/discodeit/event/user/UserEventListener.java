package com.sprint.mission.discodeit.event.user;


import com.sprint.mission.discodeit.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {


    private final SseService sseService;

    @Async("notification")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(UserEvent event) {
        sseService.broadcast(event.getName(), event.getData());
    }

}
