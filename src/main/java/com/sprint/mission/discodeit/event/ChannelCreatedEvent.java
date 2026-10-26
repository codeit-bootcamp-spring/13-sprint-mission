package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.ChannelDto;

import java.util.List;
import java.util.UUID;

public record ChannelCreatedEvent(
        ChannelDto channelDto,
        List<UUID> receiverIds
) {
}
