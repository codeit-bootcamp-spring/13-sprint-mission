package com.sprint.mission.discodeit.event;

import java.util.Set;
import java.util.UUID;

public record SseMessage(
        UUID id,
        Set<UUID> receiverIds,
        String eventName,
        Object data
) {
}
