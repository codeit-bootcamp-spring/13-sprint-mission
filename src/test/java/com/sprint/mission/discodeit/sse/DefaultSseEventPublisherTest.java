package com.sprint.mission.discodeit.sse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.NotificationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.sprint.mission.discodeit.sse.SseTestFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 실제 발행자와 ObjectMapper로 수신자 복사·ID 생성·JSON 스냅샷을 검증한다.
 * 저장소는 Mock으로 두어 저장 요청 내용을 캡처한다. 실제 저장 성공이나 Redis 발행은 별도 책임이다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("공통 SSE 발행자 단위 테스트")
class DefaultSseEventPublisherTest {
    @Mock SseMessageRepository repository;
    private final ObjectMapper mapper = objectMapper();
    private DefaultSseEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new DefaultSseEventPublisher(mapper, repository);
    }

    @Test
    @DisplayName("여러 수신자의 중복을 제거하고 하나의 ID와 JSON으로 한 번만 저장 요청한다")
    void send_createsOneMessageForAllReceivers() throws Exception {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        List<UUID> receivers = new ArrayList<>(List.of(first, second, first));
        NotificationDto notification = notification(first);

        publisher.send(receivers, "notifications.created", notification);
        // 발행 뒤 원본 수신자 목록을 바꿔도 저장 요청의 스냅샷은 유지되어야 한다.
        receivers.clear();

        ArgumentCaptor<SseMessage> captured = ArgumentCaptor.forClass(SseMessage.class);
        verify(repository).saveAndPublish(captured.capture());
        SseMessage message = captured.getValue();
        assertThat(message.eventId()).isNotNull();
        assertThat(message.eventName()).isEqualTo("notifications.created");
        assertThat(message.broadcast()).isFalse();
        assertThat(message.receiverIds()).containsExactlyInAnyOrder(first, second);
        assertThatThrownBy(() -> message.receiverIds().add(UUID.randomUUID()))
                .isInstanceOf(UnsupportedOperationException.class);
        // 문자열 전체 일치 대신 JSON 필드를 비교해 직렬화 공백이나 속성 순서에 테스트가 묶이지 않게 한다.
        JsonNode data = mapper.readTree(message.dataJson());
        assertThat(data.get("id").asText()).isEqualTo(notification.id().toString());
        assertThat(data.get("receiverId").asText()).isEqualTo(first.toString());
        assertThat(data.get("content").asText()).isEqualTo(notification.content());
        assertThat(OffsetDateTime.parse(data.get("createdAt").asText()))
                .isEqualTo(notification.createdAt());
    }

    @Test
    @DisplayName("수신자가 없으면 저장하지 않는다")
    void send_doesNothingForEmptyReceivers() {
        publisher.send(List.of(), "notifications.created", notification(UUID.randomUUID()));

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("전체 방송은 현재 연결 여부에 관계없이 복원 가능한 메시지로 저장 요청한다")
    void broadcast_persistsWithBroadcastFlagAndNoExplicitReceivers() {
        publisher.broadcast("users.updated", Map.of("online", false));

        ArgumentCaptor<SseMessage> captured = ArgumentCaptor.forClass(SseMessage.class);
        verify(repository).saveAndPublish(captured.capture());
        assertThat(captured.getValue().eventId()).isNotNull();
        assertThat(captured.getValue().broadcast()).isTrue();
        assertThat(captured.getValue().receiverIds()).isEmpty();
        assertThat(captured.getValue().eventName()).isEqualTo("users.updated");
    }

    @Test
    @DisplayName("같은 내용의 별도 발행 요청은 서로 다른 ID를 가진다")
    void send_allocatesDistinctIdsPerPublication() {
        UUID receiver = UUID.randomUUID();
        NotificationDto notification = notification(receiver);
        publisher.send(List.of(receiver), "notifications.created", notification);
        publisher.send(List.of(receiver), "notifications.created", notification);

        ArgumentCaptor<SseMessage> captured = ArgumentCaptor.forClass(SseMessage.class);
        verify(repository, times(2)).saveAndPublish(captured.capture());
        assertThat(captured.getAllValues()).extracting(SseMessage::eventId).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("JSON 변환이 실패하면 저장하거나 발행하지 않는다")
    void send_doesNotPersistWhenSerializationFails() throws Exception {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        UUID receiver = UUID.randomUUID();
        NotificationDto notification = notification(receiver);
        JsonProcessingException failure = new JsonProcessingException("cannot serialize") {};
        given(failingMapper.writeValueAsString(notification)).willThrow(failure);
        DefaultSseEventPublisher failingPublisher = new DefaultSseEventPublisher(failingMapper, repository);

        assertThatThrownBy(() -> failingPublisher.send(List.of(receiver), "notifications.created", notification))
                .isInstanceOf(IllegalStateException.class).hasCause(failure);
        verifyNoInteractions(repository);
    }
}
