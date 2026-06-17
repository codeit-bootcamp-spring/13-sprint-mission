package com.sprint.mission.discodeit.dto.channel;

import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.UUID;

public record ChannelUpdateRequest(
        UUID channelId,
        ChannelType newType,
        String newName,
        String newDescription
) { }
