package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.time.Instant;
import java.util.UUID;

public record ChannelUpdateResponse(
        UUID channelId,
        Instant createdAt,
        Instant updatedAt,
        ChannelType type,
        String name,
        String description
) {
    public static ChannelUpdateResponse from(Channel channel) {
        return new ChannelUpdateResponse(
                channel.getId(),
                channel.getCreatedAt(),
                channel.getUpdatedAt(),
                channel.getType(),
                channel.getName(),
                channel.getDescription()
        );
    }
}
