package com.sprint.mission.discodeit.dto.projection;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageProjection(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String content,
        UUID channelId,
        UUID userId,
        List<UUID> attachments
) {
}
