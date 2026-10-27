package com.sprint.mission.discodeit.websocket;

import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class WebSocketRequiredEventListener {

  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  // 메시지 저장 트랜잭션이 커밋된 후에 이벤트를 처리하도록 설정
  public void handleMessage(MessageCreatedEvent event) {
    // 클라이언트는 채널 입장 시 웹소켓으로 /sub/channels.{channelId}.messages 구독해 메시지를 수신
    messagingTemplate.convertAndSend(
        String.format("/sub/channels.%s.messages", event.data().getChannelId()),
        event.data()
    );
  }
}
