package com.sprint.mission.discodeit.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        UUID channelId,
        UUID senderId,
        String content,
        List<BinaryContentResponse> attachments,
        Instant createdAt,
        Instant updatedAt,
        UserResponse author
) {
}
