
package com.sprint.mission.discodeit.websocket;

import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketRequiredEventListenerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private WebSocketRequiredEventListener listener;

    @Test
    @DisplayName("메시지 생성 이벤트를 수신하면 해당 채널 구독자에게 메시지를 전송한다")
    void shouldSendMessageToChannelSubscribersWhenMessageCreated() {

        // given
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        MessageCreatedEvent event =
                new MessageCreatedEvent(messageId);

        MessageDto message = new MessageDto(
                messageId,
                Instant.now(),
                Instant.now(),
                "WebSocket 테스트 메시지",
                channelId,
                null,
                List.of()
        );

        when(messageService.find(messageId))
                .thenReturn(message);

        listener.handleMessage(event);

        verify(messageService).find(messageId);

        String expectedDestination =
                "/sub/channels." + channelId + ".messages";

        verify(messagingTemplate).convertAndSend(
                expectedDestination,
                message
        );

        verifyNoMoreInteractions(messagingTemplate);
    }
}
