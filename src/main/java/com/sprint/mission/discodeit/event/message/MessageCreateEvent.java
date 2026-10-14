package com.sprint.mission.discodeit.event.message;

import java.util.UUID;

public record MessageCreateEvent(
        UUID id,
        UUID channelId,
        UUID userId,
        String content
) {
}
