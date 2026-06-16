package com.sprint.mission.discodeit.dto.data;

import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.List;
import java.util.UUID;

public record ChannelDto(
        UUID id,
        String name,
        String description,
        ChannelType type,
        java.time.Instant lastMessageAt,
        List<UUID> participantsIds
) {
}
