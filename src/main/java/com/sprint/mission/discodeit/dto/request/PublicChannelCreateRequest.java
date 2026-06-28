package com.sprint.mission.discodeit.dto.request;

public record PublicChannelCreateRequest(
        String channelName,
        String description
) {
}
