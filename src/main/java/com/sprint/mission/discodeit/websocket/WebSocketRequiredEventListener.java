package com.sprint.mission.discodeit.websocket;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketRequiredEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleMessage(MessageCreatedEvent event) {

        MessageDto message = messageService.find(event.messageId());

        String destination = "/sub/channels."
                +message.channelId()
                +".messages";

        messagingTemplate.convertAndSend(destination, message);

        log.info("WebSocket 메세지 전송 완료. messageId={}, destination={}",
                event.messageId(),
                destination
        );
    }
}
