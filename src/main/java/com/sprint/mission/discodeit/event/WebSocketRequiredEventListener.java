package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.event.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WebSocketRequiredEventListener {

    // 구독된 세션에 메세지를 전송하기 위한 객체.
    private final SimpMessagingTemplate messagingTemplate;

    // /sub/channels.{channelId}.messages 에 메세지 생성시, 해당 채널의 구독자에게 메세지 발송?
    // todo - 아마 MessageDto 를 전송해야 메세지가 정상적으로 표시될 것임.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMessage(MessageCreateEvent event) {
        String url = String.format("/sub/channels.%s.messages",event.channelId());

        messagingTemplate.convertAndSend(url, event.content());
    }

}
