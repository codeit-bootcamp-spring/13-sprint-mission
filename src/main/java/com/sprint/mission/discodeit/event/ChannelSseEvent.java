package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.ChannelDto;

import java.util.Set;
import java.util.UUID;

public record ChannelSseEvent(
        String eventName,
        ChannelDto channel,
        Set<UUID> receiverIds,
        boolean broadcast
) {
    public ChannelSseEvent {
        receiverIds = Set.copyOf(receiverIds);
    }
}
