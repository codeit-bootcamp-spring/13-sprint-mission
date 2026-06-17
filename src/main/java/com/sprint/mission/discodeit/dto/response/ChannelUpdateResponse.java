package com.sprint.mission.discodeit.dto.response;

import com.sprint.mission.discodeit.dto.request.ChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.UUID;

public record ChannelUpdateResponse(
        UUID channelId,
        ChannelType type,
        String name,
        String description
) {
    public static ChannelUpdateResponse from(Channel channel) {
        return new ChannelUpdateResponse(
                channel.getId(),
                channel.getType(),
                channel.getName(),
                channel.getDescription()
        );
    }
}
