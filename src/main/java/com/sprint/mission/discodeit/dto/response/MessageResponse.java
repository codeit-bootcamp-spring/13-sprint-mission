package com.sprint.mission.discodeit.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID channelId,
        UUID senderId,
        String content,
        List<UUID> binaryContentIds,
        Instant createdAt,
        Instant updatedAt
) {
}
