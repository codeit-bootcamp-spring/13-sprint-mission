package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.service.MessageService;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WebSocketRequiredEventListenerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketRequiredEventListener listener;

    @Test
    void handleMessage_채널_구독자에게_메시지를_전송한다() {
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        MessageCreatedEvent event =
                new MessageCreatedEvent(messageId);

        MessageResponse message =
                new MessageResponse(
                        messageId,
                        channelId,
                        UUID.randomUUID(),
                        "새 메시지",
                        List.of(),
                        Instant.now(),
                        Instant.now(),
                        null
                );

        given(messageService.findById(messageId))
                .willReturn(Optional.of(message));

        listener.handleMessage(event);

        then(messagingTemplate)
                .should()
                .convertAndSend(
                        "/sub/channels.%s.messages"
                                .formatted(channelId),
                        message
                );
    }

    @Test
    void handleMessage_메시지가_없으면_실패한다() {
        UUID messageId = UUID.randomUUID();

        MessageCreatedEvent event =
                new MessageCreatedEvent(messageId);

        given(messageService.findById(messageId))
                .willReturn(Optional.empty());

        assertThatThrownBy(
                () -> listener.handleMessage(event)
        )
                .isInstanceOf(
                        MessageNotFoundException.class
                )
                .hasMessage("메시지를 찾을 수 없습니다.");

        verifyNoInteractions(messagingTemplate);
    }
}