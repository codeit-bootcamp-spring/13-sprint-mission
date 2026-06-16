package com.sprint.mission.discodeit.dto.response;

import java.time.Instant;
import java.util.UUID;

// [요구사항] 수정 불가능한 도메인이므로 updatedAt 필드는 제외
public record BinaryContentResponse(
        UUID id,
        Instant createdAt,
        UUID userId,
        UUID messageId,
        String fileName
) {}
