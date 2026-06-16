package com.sprint.mission.discodeit.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        String name,
        String description,
        String type,
        Instant lastMessageAt,
        List<UUID> memberIds
) {
}
