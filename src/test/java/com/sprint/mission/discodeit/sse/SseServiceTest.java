package com.sprint.mission.discodeit.sse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.sprint.mission.discodeit.sse.SseTestFixtures.delivery;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 실제 SSE 서비스의 연결 등록·복원 요청·실패 연결 제거 계약을 검증한다.
 * Reader와 저장소, 전송 분기의 연결은 Mock이며 connect가 만드는 연결은 실제 객체다.
 * 실제 HTTP 스트림이나 브라우저 수신을 검증하는 테스트는 아니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SSE 연결 관리 및 전송 서비스 단위 테스트")
class SseServiceTest {
    @Mock SseMessageReader messageReader;
    @Mock SseConnectionRepository connectionRepository;
    @Mock SseConnection firstConnection;
    @Mock SseConnection secondConnection;
    @InjectMocks SseService service;

    @Test
    @DisplayName("지정한 사용자의 모든 연결에 동일한 Delivery를 전달한다")
    void send_deliversToAllConnectionsOfEachReceiver() throws IOException {
        UUID firstUser = UUID.randomUUID();
        UUID secondUser = UUID.randomUUID();
        SseConnection thirdConnection = mock(SseConnection.class);
        SseDelivery delivery = delivery(1);
        given(connectionRepository.get(firstUser)).willReturn(List.of(firstConnection, secondConnection));
        given(connectionRepository.get(secondUser)).willReturn(List.of(thirdConnection));

        service.send(List.of(firstUser, secondUser), delivery);

        verify(firstConnection).send(same(delivery));
        verify(secondConnection).send(same(delivery));
        verify(thirdConnection).send(same(delivery));
    }

    @ParameterizedTest
    @MethodSource("sendFailures")
    @DisplayName("실패한 연결만 제거하고 다른 연결에는 계속 전달한다")
    void send_removesOnlyFailedConnection(Exception failure) throws IOException {
        UUID receiverId = UUID.randomUUID();
        SseDelivery delivery = delivery(1);
        given(connectionRepository.get(receiverId)).willReturn(List.of(firstConnection, secondConnection));
        doThrow(failure).when(firstConnection).send(delivery);

        assertThatCode(() -> service.send(List.of(receiverId), delivery)).doesNotThrowAnyException();

        verify(connectionRepository).remove(receiverId, firstConnection);
        verify(secondConnection).send(delivery);
        verify(connectionRepository, never()).remove(receiverId, secondConnection);
    }

    @Test
    @DisplayName("연결이 없는 수신자에게 전달해도 실패하지 않는다")
    void send_allowsReceiverWithoutConnections() {
        UUID receiverId = UUID.randomUUID();
        given(connectionRepository.get(receiverId)).willReturn(List.of());

        assertThatCode(() -> service.send(List.of(receiverId), delivery(1))).doesNotThrowAnyException();

        verifyNoInteractions(firstConnection, secondConnection);
    }

    @Test
    @DisplayName("전체 방송은 현재 서버에 연결된 모든 사용자에게 전달한다")
    void broadcast_deliversToAllLocalReceivers() throws IOException {
        UUID firstUser = UUID.randomUUID();
        UUID secondUser = UUID.randomUUID();
        SseDelivery delivery = delivery(1);
        given(connectionRepository.getAllIds()).willReturn(Set.of(firstUser, secondUser));
        given(connectionRepository.get(firstUser)).willReturn(List.of(firstConnection));
        given(connectionRepository.get(secondUser)).willReturn(List.of(secondConnection));

        service.broadcast(delivery);

        verify(firstConnection).send(same(delivery));
        verify(secondConnection).send(same(delivery));
    }

    @Test
    @DisplayName("최초 연결은 등록된 연결의 Emitter를 반환하고 과거 이력은 조회하지 않는다")
    void connect_registersNewConnectionWithoutReplayingHistory() {
        UUID receiverId = UUID.randomUUID();
        given(messageReader.findSequence(null)).willReturn(OptionalLong.empty());

        SseEmitter emitter = service.connect(receiverId, null);

        ArgumentCaptor<SseConnection> connection = ArgumentCaptor.forClass(SseConnection.class);
        verify(connectionRepository).add(eq(receiverId), connection.capture());
        assertThat(emitter).isSameAs(connection.getValue().getEmitter());
        verify(messageReader, never()).findDeliveriesAfter(any(), any());
    }

    @Test
    @DisplayName("재연결은 실시간 메시지를 받을 연결을 등록한 뒤 복원 이력을 조회한다")
    void connect_registersBeforeLoadingReplay() {
        UUID receiverId = UUID.randomUUID();
        UUID lastEventId = UUID.randomUUID();
        given(messageReader.findSequence(lastEventId)).willReturn(OptionalLong.of(7));
        given(messageReader.findDeliveriesAfter(receiverId, lastEventId)).willReturn(List.of(delivery(8)));

        SseEmitter emitter = service.connect(receiverId, lastEventId);

        ArgumentCaptor<SseConnection> connection = ArgumentCaptor.forClass(SseConnection.class);
        // 조회 전에 등록해야 조회 중 발생한 실시간 메시지를 버퍼링할 수 있다.
        var order = inOrder(connectionRepository, messageReader);
        order.verify(connectionRepository).add(eq(receiverId), connection.capture());
        order.verify(messageReader).findDeliveriesAfter(receiverId, lastEventId);
        assertThat(emitter).isSameAs(connection.getValue().getEmitter());
    }

    @Test
    @DisplayName("복원 조회가 실패하면 등록한 연결을 제거하고 원인을 전달한다")
    void connect_removesRegisteredConnectionWhenReplayFails() {
        UUID receiverId = UUID.randomUUID();
        UUID lastEventId = UUID.randomUUID();
        RuntimeException failure = new IllegalStateException("history unavailable");
        given(messageReader.findSequence(lastEventId)).willReturn(OptionalLong.of(7));
        given(messageReader.findDeliveriesAfter(receiverId, lastEventId)).willThrow(failure);

        assertThatThrownBy(() -> service.connect(receiverId, lastEventId))
                .isInstanceOf(IllegalStateException.class)
                .hasCause(failure);

        ArgumentCaptor<SseConnection> connection = ArgumentCaptor.forClass(SseConnection.class);
        verify(connectionRepository).add(eq(receiverId), connection.capture());
        verify(connectionRepository).remove(receiverId, connection.getValue());
    }

    @Test
    @DisplayName("커서 순번 조회가 실패하면 연결을 등록하지 않는다")
    void connect_doesNotRegisterWhenCursorLookupFails() {
        UUID lastEventId = UUID.randomUUID();
        given(messageReader.findSequence(lastEventId)).willThrow(new IllegalStateException("history unavailable"));

        assertThatThrownBy(() -> service.connect(UUID.randomUUID(), lastEventId))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(connectionRepository);
    }

    @ParameterizedTest
    @MethodSource("sendFailures")
    @DisplayName("정리 작업은 ping이 실패한 연결만 제거한다")
    void cleanUp_removesOnlyUnresponsiveConnections(Exception failure) throws IOException {
        UUID receiverId = UUID.randomUUID();
        SseEmitter failedEmitter = mock(SseEmitter.class);
        SseEmitter healthyEmitter = mock(SseEmitter.class);
        given(connectionRepository.getAll()).willReturn(Map.of(receiverId, List.of(firstConnection, secondConnection)));
        given(firstConnection.getEmitter()).willReturn(failedEmitter);
        given(secondConnection.getEmitter()).willReturn(healthyEmitter);
        doThrow(failure).when(failedEmitter).send(any(SseEmitter.SseEventBuilder.class));

        service.cleanUp();

        verify(connectionRepository).remove(receiverId, firstConnection);
        verify(connectionRepository, never()).remove(receiverId, secondConnection);
        verify(healthyEmitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    private static Stream<Exception> sendFailures() {
        return Stream.of(new IOException("connection closed"), new IllegalStateException("emitter completed"));
    }
}
