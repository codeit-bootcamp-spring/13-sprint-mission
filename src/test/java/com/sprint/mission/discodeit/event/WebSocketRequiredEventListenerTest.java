package com.sprint.mission.discodeit.event;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class WebSocketRequiredEventListenerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WebSocketRequiredEventListener listener;

    @Test
    @DisplayName("메시지 생성 이벤트를 지정된 Kafka 토픽으로 발행")
    void handleMessage_success() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();

        MessageCreatedEvent event = new MessageCreatedEvent(messageId);

        String payload = """
                {"messageId":"%s"}
                """.formatted(messageId).trim();

        given(objectMapper.writeValueAsString(event))
                .willReturn(payload);

        // when
        listener.handleMessage(event);

        // then
        then(objectMapper).should()
                .writeValueAsString(event);

        then(kafkaTemplate).should()
                .send("discodeit.websocket.messages", payload);
    }

    @Test
    @DisplayName("메시지 생성 이벤트 직렬화 실패 시 예외 발생")
    void handleMessage_serialization_failure() throws Exception {
        // given
        MessageCreatedEvent event =
                new MessageCreatedEvent(UUID.randomUUID());

        given(objectMapper.writeValueAsString(event))
                .willThrow(new JsonProcessingException("직렬화 실패") {});

        // when & then
        assertThatThrownBy(() -> listener.handleMessage(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("WebSocket 이벤트 직렬화에 실패했습니다.")
                .hasCauseInstanceOf(JsonProcessingException.class);

        then(kafkaTemplate).shouldHaveNoInteractions();
    }
}