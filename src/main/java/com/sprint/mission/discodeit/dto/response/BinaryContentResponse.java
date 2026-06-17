package com.sprint.mission.discodeit.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record BinaryContentResponse(
        UUID id,
        LocalDateTime createdAt,
        String fileName,
        String fileUrl,
        Long fileSize,
        UUID messageId
) {
}
