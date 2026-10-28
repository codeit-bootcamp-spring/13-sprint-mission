package com.sprint.mission.discodeit.websocket;

import com.sprint.mission.discodeit.events.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 메시지 저장 트랜잭션이 커밋된 뒤 채팅 DTO를 발행한다.
 * 롤백된 메시지가 먼저 화면에 나타나지 않게 하며, 이벤트에 담긴 DTO를 그대로 사용한다.
 * 커밋 이후 발행 실패까지 DB와 함께 복구하는 outbox 처리는 포함하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

    private final WebSocketMessagePublisher webSocketMessagePublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessage(MessageCreatedEvent event) {
        webSocketMessagePublisher.publish(event.messageDto());
    }
}
