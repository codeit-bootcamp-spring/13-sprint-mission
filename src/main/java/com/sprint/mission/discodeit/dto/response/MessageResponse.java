package com.sprint.mission.discodeit.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID userId,
        UUID channelId,
        String content,
        List<UUID> binaryContentIds
) {
}
