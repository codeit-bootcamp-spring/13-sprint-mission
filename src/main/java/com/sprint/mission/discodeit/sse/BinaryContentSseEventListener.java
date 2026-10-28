package com.sprint.mission.discodeit.sse;

import com.sprint.mission.discodeit.event.BinaryContentUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinaryContentSseEventListener {

    private final SseService sseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBinaryContentUpdated(
            BinaryContentUpdatedEvent event
    ) {

        sseService.broadcast(
                "binaryContents.updated",
                event.binaryContent()
        );

        log.info(
                "파일 상태 변경 SSE 이벤트 전송 완료. binaryContentId={}",
                event.binaryContent().id()
        );
    }
}
