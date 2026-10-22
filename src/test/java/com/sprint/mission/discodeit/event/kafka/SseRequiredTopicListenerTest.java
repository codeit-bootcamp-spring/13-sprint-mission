package com.sprint.mission.discodeit.event.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.service.SseService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SseRequiredTopicListenerTest {

    @Mock
    private SseService sseService;

    private ObjectMapper objectMapper;
    private SseRequiredTopicListener listener;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        listener = new SseRequiredTopicListener(
                objectMapper,
                sseService
        );
    }

    @Test
    @DisplayName("전체 SSE 이벤트 수신 시 broadcast 호출")
    void onSseEvent_broadcast_success() throws Exception {
        // given
        String eventName = "userUpdated";
        JsonNode data = objectMapper.readTree("""
                {"message":"사용자 정보 변경"}
                """);

        SseKafkaEvent event = new SseKafkaEvent(
                null,
                eventName,
                data
        );

        String payload = objectMapper.writeValueAsString(event);

        // when
        listener.onSseEvent(payload);

        // then
        then(sseService).should()
                .broadcast(eventName, data);

        then(sseService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("특정 사용자 SSE 이벤트 수신 시 해당 사용자에게 send 호출")
    void onSseEvent_send_success() throws Exception {
        // given
        UUID receiverId1 = UUID.randomUUID();
        UUID receiverId2 = UUID.randomUUID();

        List<UUID> receiverIds = List.of(
                receiverId1,
                receiverId2
        );

        String eventName = "notificationCreated";

        JsonNode data = objectMapper.readTree("""
                {"message":"새로운 알림"}
                """);

        SseKafkaEvent event = new SseKafkaEvent(
                receiverIds,
                eventName,
                data
        );

        String payload = objectMapper.writeValueAsString(event);

        // when
        listener.onSseEvent(payload);

        // then
        then(sseService).should()
                .send(receiverIds, eventName, data);

        then(sseService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("잘못된 Kafka JSON 수신 시 예외 발생 및 SSE 전송 방지")
    void onSseEvent_invalid_json() {
        // given
        String invalidPayload = "{invalid-json}";

        // when & then
        assertThatThrownBy(
                () -> listener.onSseEvent(invalidPayload)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SSE Kafka 이벤트 역직렬화에 실패했습니다.");

        then(sseService).shouldHaveNoInteractions();
    }
}