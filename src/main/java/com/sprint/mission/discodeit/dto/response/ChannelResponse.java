package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.Channel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelResponse(
        UUID id,
        String name,
        Channel.ChannelType type,
        String description,
        Instant latestMessageAt,
        List<UUID> participantIds
) {
    public static ChannelResponse from(
            Channel channel,
            Instant latestMessageAt,
            List<UUID> participantIds
    ) {
        return new ChannelResponse(
                channel.getId(),
                channel.getName(),
                channel.getType(),
                channel.getDescription(),
                latestMessageAt,
                participantIds
        );
    }
}
