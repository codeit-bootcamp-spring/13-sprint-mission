package com.sprint.mission.discodeit.realtime;

import com.sprint.mission.discodeit.event.message.MessageCreatedEvent;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.*;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {
  private final MessageService messages;
  private final RealtimePublisher realtime;

  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
  public void handleMessage(MessageCreatedEvent event) {
    realtime.websocket("/sub/channels." + event.channelId() + ".messages",
        messages.findMessageById(event.messageId()));
  }
}
