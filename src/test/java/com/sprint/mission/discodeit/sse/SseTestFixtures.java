package com.sprint.mission.discodeit.sse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sprint.mission.discodeit.dto.response.NotificationDto;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * SSE 테스트에서 사용할 실제 DTO·이벤트·전송 자료와 JSON 매퍼를 생성한다.
 * 고정된 순번과 수신 대상을 통해 저장·복원·전송 계약을 각각 비교할 수 있게 한다.
 */
final class SseTestFixtures {
    private SseTestFixtures() {}

    static ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    static NotificationDto notification(UUID receiverId) {
        return new NotificationDto(UUID.randomUUID(), OffsetDateTime.parse("2026-10-18T00:00:00Z"),
                receiverId, "새 알림", "알림 내용");
    }

    static SseMessage message(UUID receiverId) {
        return new SseMessage(UUID.randomUUID(), "notifications.created",
                "{\"content\":\"알림 내용\"}", Set.of(receiverId), false);
    }

    static SseDelivery delivery(long sequence) {
        return new SseDelivery(UUID.randomUUID(), sequence, "notifications.created",
                Map.of("content", "알림 " + sequence));
    }
}
