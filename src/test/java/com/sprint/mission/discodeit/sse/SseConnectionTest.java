package com.sprint.mission.discodeit.sse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sprint.mission.discodeit.sse.SseTestFixtures.delivery;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 실제 연결 객체의 복원 대기열과 전송 순번 상태를 검증한다.
 * 네트워크에 쓰는 Emitter만 Mock으로 두고 전송된 SSE 프레임의 ID·이름·본문과 순서를 관찰한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("실제 SSE 연결의 복원·전송 상태 테스트")
class SseConnectionTest {
    @Mock SseEmitter emitter;

    @Test
    @DisplayName("복원 중 실시간 메시지를 보관하고 이력과 중복을 제거하여 순번대로 전달한다")
    void completeReplay_mergesPendingAndHistoryInSequenceOrder() throws IOException {
        SseConnection connection = new SseConnection(emitter, 5);
        SseDelivery sixth = delivery(6);
        SseDelivery seventh = delivery(7);
        SseDelivery eighth = delivery(8);
        connection.send(eighth);
        connection.send(sixth);
        connection.send(sixth);
        verifyNoInteractions(emitter);

        connection.completeReplay(List.of(seventh, sixth, delivery(5)));

        // 복원 이력과 실시간 대기열이 겹쳐도 중복 없이 6, 7, 8 순서로 전달되는 것이 검증 대상이다.
        List<SseEmitter.SseEventBuilder> sent = capturedEvents(3);
        assertEvent(sent.get(0), sixth);
        assertEvent(sent.get(1), seventh);
        assertEvent(sent.get(2), eighth);
    }

    @Test
    @DisplayName("복원 완료 후 새로운 메시지는 즉시 보내고 이전·중복 순번은 제외한다")
    void send_deliversLiveMessagesOnceAfterReplay() throws IOException {
        SseConnection connection = new SseConnection(emitter, 5);
        SseDelivery sixth = delivery(6);
        SseDelivery seventh = delivery(7);
        connection.completeReplay(List.of(sixth));

        connection.send(sixth);
        connection.send(delivery(4));
        connection.send(seventh);
        connection.send(seventh);

        List<SseEmitter.SseEventBuilder> sent = capturedEvents(2);
        assertEvent(sent.get(0), sixth);
        assertEvent(sent.get(1), seventh);
    }

    @Test
    @DisplayName("빈 복원 결과로도 실시간 전송 상태로 전환한다")
    void completeReplay_withEmptyHistoryEnablesLiveDelivery() throws IOException {
        SseConnection connection = new SseConnection(emitter, 0);
        connection.completeReplay(List.of());
        SseDelivery delivery = delivery(1);

        connection.send(delivery);

        assertEvent(capturedEvents(1).get(0), delivery);
    }

    @Test
    @DisplayName("닫힌 연결은 보관 중이거나 새로 도착한 메시지를 보내지 않는다")
    void close_preventsBufferedAndFutureDelivery() throws IOException {
        SseConnection connection = new SseConnection(emitter, 0);
        connection.send(delivery(1));

        connection.close();
        connection.send(delivery(2));

        assertThatThrownBy(() -> connection.completeReplay(List.of(delivery(3))))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(emitter);
    }

    @ParameterizedTest
    @MethodSource("sendFailures")
    @DisplayName("전송 실패를 호출자에게 알리고 해당 연결에서 추가 전송을 차단한다")
    void send_closesConnectionAfterFailure(Exception failure) throws IOException {
        SseConnection connection = new SseConnection(emitter, 0);
        connection.completeReplay(List.of());
        doThrow(failure).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        assertThatThrownBy(() -> connection.send(delivery(1))).isSameAs(failure);
        connection.send(delivery(2));

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("복원 도중 전송이 실패하면 남은 버퍼와 이후 실시간 메시지를 보내지 않는다")
    void completeReplay_stopsAfterSendFailure() throws IOException {
        SseConnection connection = new SseConnection(emitter, 0);
        connection.send(delivery(3));
        IOException failure = new IOException("connection lost");
        doThrow(failure).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

        assertThatThrownBy(() -> connection.completeReplay(List.of(delivery(1), delivery(2))))
                .isSameAs(failure);
        connection.send(delivery(4));

        verify(emitter).send(any(SseEmitter.SseEventBuilder.class));
    }

    @Test
    @DisplayName("완료된 복원을 다시 수행하지 않는다")
    void completeReplay_rejectsSecondCompletion() throws IOException {
        SseConnection connection = new SseConnection(emitter, 0);
        connection.completeReplay(List.of());

        assertThatThrownBy(() -> connection.completeReplay(List.of(delivery(1))))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(emitter);
    }

    private List<SseEmitter.SseEventBuilder> capturedEvents(int count) throws IOException {
        ArgumentCaptor<SseEmitter.SseEventBuilder> captured = ArgumentCaptor.forClass(SseEmitter.SseEventBuilder.class);
        verify(emitter, times(count)).send(captured.capture());
        return captured.getAllValues();
    }

    private void assertEvent(SseEmitter.SseEventBuilder event, SseDelivery expected) {
        List<Object> parts = event.build().stream()
                .map(ResponseBodyEmitter.DataWithMediaType::getData).toList();
        String fields = parts.stream().filter(String.class::isInstance)
                .map(String.class::cast).collect(Collectors.joining());
        // 프레임 문자열이 나뉘는 내부 구현 대신 클라이언트에 전달되는 필드를 검증한다.
        assertThat(fields.lines().filter(line -> line.startsWith("id:") || line.startsWith("event:")).toList())
                .containsExactlyInAnyOrder("id:" + expected.eventId(), "event:" + expected.eventName());
        assertThat(parts).filteredOn(part -> !(part instanceof String)).containsExactly(expected.data());
    }

    private static Stream<Exception> sendFailures() {
        return Stream.of(new IOException("connection lost"), new IllegalStateException("emitter completed"));
    }
}
