package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.ChannelType;

public record PublicChannelCreateRequest(
        ChannelType type,
        String name,
        String description
) {
}
