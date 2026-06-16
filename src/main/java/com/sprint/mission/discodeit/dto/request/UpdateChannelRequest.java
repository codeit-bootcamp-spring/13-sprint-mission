package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.UUID;

public record UpdateChannelRequest(
        UUID channelId,
        String name,
        Channel.ChannelType type,
        String description
) {
}
