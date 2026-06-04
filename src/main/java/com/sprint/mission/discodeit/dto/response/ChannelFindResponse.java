package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChannelFindResponse(
        ChannelType type,
        String name,
        String description,
        Instant recentMessageTime,
        List<UUID> usersId
) {
    public static ChannelFindResponse from(Channel channel, Instant recentMessageTime, List<UUID> usersId) {
        return new ChannelFindResponse(
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                recentMessageTime,
                usersId
        );
    }
}
