package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.event.ChannelSseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelSseEventListener {

    private final SseService sseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChannelChanged(ChannelSseEvent event) {

        if (event.broadcast()) {
            sseService.broadcast(
                    event.eventName(),
                    event.channel()
            );
        } else {
            for (UUID receiverId : event.receiverIds()) {
                sseService.send(
                        receiverId,
                        event.eventName(),
                        event.channel()
                );
            }
        }

        log.info(
                "채널 SSE 이벤트 처리 완료. eventName={}",
                event.eventName()
        );
    }
}
