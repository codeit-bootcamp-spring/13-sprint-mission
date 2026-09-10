package com.sprint.mission.discodeit.dto.projection;

import com.sprint.mission.discodeit.entity.ChannelType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelProjection(
        UUID id,
        ChannelType type,
        String name,
        String description,
        List<UUID> userIds,
        Instant lastMessageAt
) {
    /**
     * return user id list to variable param method
     * @return UUID[] userIds
     */
    public UUID[] users(){
        return userIds.toArray(new UUID[0]);
    }
}
