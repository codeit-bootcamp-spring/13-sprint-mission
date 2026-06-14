package com.sprint.mission.discodeit.dto.response;

import java.util.UUID;

public record ChannelResponse(
        UUID channelIds,
        String name,
        String description
) {}
