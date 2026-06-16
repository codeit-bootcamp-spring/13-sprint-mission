package com.sprint.mission.discodeit.dto.request;

import java.util.List;
import java.util.UUID;

public record MessageCreateRequest(
        UUID userId,
        UUID channelId,
        String content,
        List<UUID> binaryContentIds
) {
}
