package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.event.ChannelSseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelSseEventListener {

    private final SseService sseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleChannelChanged(ChannelSseEvent event) {

        if (event.broadcast()) {
            // 공개 채널: 모든 SSE 연결 사용자에게 전송
            sseService.broadcast(
                    event.eventName(),
                    event.channel()
            );
        } else {
            // 비공개 채널: 지정된 참여자들에게만 전송
            sseService.send(
                    event.receiverIds(),
                    event.eventName(),
                    event.channel()
            );
        }

        log.info(
                "채널 SSE 이벤트 처리 완료. eventName={}",
                event.eventName()
        );
    }
}
