package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.ChannelType;

public record ChannelUpdateRequest(
        ChannelType type,
        String name,
        String description
) {
}
