package com.sprint.mission.discodeit.dto.response;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentResponse(
        UUID id,
        Instant createdAt,
        String fileName,
        String fileUrl,
        Long fileSize,
        UUID messageId
) {
}
