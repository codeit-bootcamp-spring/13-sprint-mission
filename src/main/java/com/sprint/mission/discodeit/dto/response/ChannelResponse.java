package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.Channel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        String name,
        String description,
        Channel.ChannelType type,
        Instant lastMessageAt,
        List<UUID> memberIds
) {
}
