package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.dto.response.ChannelDto;

public record ChannelUpdatedEvent(
        ChannelDto channelDto
) {
}
