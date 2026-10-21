package com.sprint.mission.discodeit.sse;

import java.time.Instant;
import java.util.UUID;

public record SseMessage(
        UUID id,
        UUID userId,
        String eventName,
        Object data,
        Instant createdAt
) {
}
