package com.sprint.mission.discodeit.dto.request;

import java.time.Instant;
import java.util.UUID;

public record BinaryContentCreateRequest(
        String fileName,
        String fileUrl,
        Long fileSize
) {
}
