package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handleMessage(MessageCreatedEvent event) {
        MessageResponse message = messageService
                .findById(event.messageId())
                .orElseThrow(
                        () -> new MessageNotFoundException(
                                event.messageId()
                        )
                );

        String destination =
                "/sub/channels.%s.messages"
                        .formatted(message.channelId());

        messagingTemplate.convertAndSend(
                destination,
                message
        );
    }
}