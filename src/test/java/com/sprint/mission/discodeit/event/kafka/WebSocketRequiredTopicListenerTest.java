package com.sprint.mission.discodeit.event.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.event.MessageCreatedEvent;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class WebSocketRequiredTopicListenerTest {

    @Mock
    private MessageService messageService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    private ObjectMapper objectMapper;
    private WebSocketRequiredTopicListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        listener = new WebSocketRequiredTopicListener(
                objectMapper,
                messageService,
                simpMessagingTemplate
        );
    }

    @Test
    @DisplayName("Kafka 메시지 수신 시 해당 채널의 WebSocket 구독 경로로 메시지를 전송")
    void onMessageCreatedEvent_success() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        MessageCreatedEvent event = new MessageCreatedEvent(messageId);
        String payload = objectMapper.writeValueAsString(event);

        MessageDto messageDto = org.mockito.Mockito.mock(MessageDto.class);

        given(messageDto.channelId())
                .willReturn(channelId);

        given(messageService.getMessage(messageId))
                .willReturn(messageDto);

        String expectedDestination =
                "/sub/channels." + channelId + ".messages";

        // when
        listener.onMessageCreatedEvent(payload);

        // then
        then(messageService).should()
                .getMessage(messageId);

        then(simpMessagingTemplate).should()
                .convertAndSend(expectedDestination, messageDto);
    }

    @Test
    @DisplayName("Kafka 이벤트 역직렬화 실패 시 예외를 발생시키고 WebSocket 메시지를 전송하지 않음")
    void onMessageCreatedEvent_invalid_json() {
        // given
        String invalidPayload = "{invalid-json}";

        // when & then
        assertThatThrownBy(
                () -> listener.onMessageCreatedEvent(invalidPayload)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WebSocket 이벤트 역직렬화에 실패했습니다.");

        then(messageService).shouldHaveNoInteractions();
        then(simpMessagingTemplate).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("메시지 조회에 실패하면 WebSocket 메시지를 전송하지 않음")
    void onMessageCreatedEvent_message_not_found() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();

        MessageCreatedEvent event = new MessageCreatedEvent(messageId);
        String payload = objectMapper.writeValueAsString(event);

        given(messageService.getMessage(messageId))
                .willThrow(new IllegalArgumentException("메시지를 찾을 수 없습니다."));

        // when & then
        assertThatThrownBy(
                () -> listener.onMessageCreatedEvent(payload)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("메시지를 찾을 수 없습니다.");

        then(simpMessagingTemplate).shouldHaveNoInteractions();
    }
}