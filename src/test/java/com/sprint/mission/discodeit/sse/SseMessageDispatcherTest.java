package com.sprint.mission.discodeit.sse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static com.sprint.mission.discodeit.sse.SseTestFixtures.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 실제 전달기가 지정 수신자와 전체 방송을 구분하는지 검증한다.
 * Reader와 전송 서비스는 Mock으로 두고 변환한 동일 Delivery가 선택한 경로로 전달되는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("공통 SSE 메시지 전달 분기 테스트")
class SseMessageDispatcherTest {
    @Mock SseMessageReader reader;
    @Mock SseService service;
    @InjectMocks SseMessageDispatcher dispatcher;

    @Test
    @DisplayName("지정된 모든 수신자에게 Reader가 반환한 동일한 Delivery를 전달한다")
    void handle_sendsOnlyToExplicitReceivers() {
        Set<UUID> receivers = Set.of(UUID.randomUUID(), UUID.randomUUID());
        SseMessage message = new SseMessage(UUID.randomUUID(), "channels.updated", "{}", receivers, false);
        SseDelivery delivery = new SseDelivery(message.eventId(), 2, message.eventName(), objectMapper().createObjectNode());
        given(reader.toDelivery(message)).willReturn(delivery);

        dispatcher.handle(message);

        verify(service).send(eq(receivers), same(delivery));
        // 대상 외 방송이나 이중 전송은 수신 범위 계약 위반이다.
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("전체 방송 메시지는 동일한 Delivery로 한 번 방송한다")
    void handle_broadcastsDelivery() {
        SseMessage message = new SseMessage(UUID.randomUUID(), "users.updated", "{}", Set.of(), true);
        SseDelivery delivery = new SseDelivery(message.eventId(), 3, message.eventName(), objectMapper().createObjectNode());
        given(reader.toDelivery(message)).willReturn(delivery);

        dispatcher.handle(message);

        verify(service).broadcast(same(delivery));
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("Delivery 변환이 실패하면 전송하지 않는다")
    void handle_stopsWhenConversionFails() {
        SseMessage message = message(UUID.randomUUID());
        IllegalStateException failure = new IllegalStateException("missing sequence");
        given(reader.toDelivery(message)).willThrow(failure);

        assertThatThrownBy(() -> dispatcher.handle(message)).isSameAs(failure);
        verifyNoInteractions(service);
    }
}
