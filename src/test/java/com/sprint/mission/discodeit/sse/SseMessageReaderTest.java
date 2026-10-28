package com.sprint.mission.discodeit.sse;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;

import static com.sprint.mission.discodeit.sse.SseTestFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

/**
 * 실제 Reader와 JSON 변환을 실행해 저장된 메타데이터와 본문 복원을 검증한다.
 * 저장소는 Mock이므로 수신자 필터링·영속 저장 대신 반환 순서 유지와 변환 오류 처리를 확인한다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("공통 SSE 메시지 조회 및 변환 단위 테스트")
class SseMessageReaderTest {
    @Mock SseMessageRepository repository;
    private SseMessageReader reader;

    @BeforeEach
    void setUp() {
        reader = new SseMessageReader(repository, objectMapper());
    }

    @Test
    @DisplayName("저장된 ID와 순번을 사용하고 JSON 데이터를 객체 노드로 복원한다")
    void toDelivery_restoresStoredMetadataAndJsonObject() {
        SseMessage message = message(UUID.randomUUID());
        given(repository.findSequence(message.eventId())).willReturn(OptionalLong.of(23));

        SseDelivery result = reader.toDelivery(message);

        assertThat(result.eventId()).isEqualTo(message.eventId());
        assertThat(result.sequence()).isEqualTo(23);
        assertThat(result.eventName()).isEqualTo(message.eventName());
        assertThat(result.data()).isInstanceOf(JsonNode.class);
        assertThat(((JsonNode) result.data()).get("content").asText()).isEqualTo("알림 내용");
    }

    @Test
    @DisplayName("커서 이후 조회 결과의 순서를 유지한다")
    void findDeliveriesAfter_preservesRepositoryOrder() {
        UUID receiver = UUID.randomUUID();
        UUID cursor = UUID.randomUUID();
        SseMessage first = message(receiver);
        SseMessage second = message(receiver);
        given(repository.findAllAfter(receiver, cursor)).willReturn(List.of(first, second));
        given(repository.findSequence(first.eventId())).willReturn(OptionalLong.of(4));
        given(repository.findSequence(second.eventId())).willReturn(OptionalLong.of(9));

        List<SseDelivery> deliveries = reader.findDeliveriesAfter(receiver, cursor);

        assertThat(deliveries).extracting(SseDelivery::eventId).containsExactly(first.eventId(), second.eventId());
        assertThat(deliveries).extracting(SseDelivery::sequence).containsExactly(4L, 9L);
    }

    @Test
    @DisplayName("복원할 이력이 없으면 빈 목록을 반환한다")
    void findDeliveriesAfter_returnsEmptyHistory() {
        UUID receiver = UUID.randomUUID();
        UUID cursor = UUID.randomUUID();
        given(repository.findAllAfter(receiver, cursor)).willReturn(List.of());

        assertThat(reader.findDeliveriesAfter(receiver, cursor)).isEmpty();
    }

    @Test
    @DisplayName("커서의 저장 순번과 알 수 없는 커서의 빈 결과를 전달한다")
    void findSequence_preservesMissingAndPresentSequence() {
        UUID eventId = UUID.randomUUID();
        given(repository.findSequence(eventId)).willReturn(OptionalLong.of(42));
        given(repository.findSequence(null)).willReturn(OptionalLong.empty());

        assertThat(reader.findSequence(eventId)).hasValue(42);
        assertThat(reader.findSequence(null)).isEmpty();
    }

    @Test
    @DisplayName("메시지의 순번이 없으면 잘못된 Delivery를 만들지 않는다")
    void toDelivery_rejectsMissingSequence() {
        SseMessage message = message(UUID.randomUUID());
        given(repository.findSequence(message.eventId())).willReturn(OptionalLong.empty());

        assertThatThrownBy(() -> reader.toDelivery(message))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(message.eventId().toString());
    }

    @Test
    @DisplayName("잘못된 데이터 JSON은 복원 실패로 알린다")
    void toDelivery_rejectsMalformedJson() {
        SseMessage message = new SseMessage(UUID.randomUUID(), "users.updated", "{invalid", Set.of(), true);
        given(repository.findSequence(message.eventId())).willReturn(OptionalLong.of(1));

        assertThatThrownBy(() -> reader.toDelivery(message))
                .isInstanceOf(IllegalStateException.class)
                .hasCauseInstanceOf(com.fasterxml.jackson.core.JsonProcessingException.class);
    }
}
