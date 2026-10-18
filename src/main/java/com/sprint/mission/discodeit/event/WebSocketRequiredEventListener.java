package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageService messageService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessage(MessageCreatedEvent event) {

        MessageDto message = messageService.getMessage(event.messageId());

        String destination = "/sub/channels." + message.channelId() + ".messages";

        simpMessagingTemplate.convertAndSend(destination, message);
    }
}
