package com.sprint.mission.discodeit.sse;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public record SseMessage(
        UUID id,
        Set<UUID> receiverIds,
        String eventName,
        Object data
) {

    public SseMessage(
            UUID id,
            Collection<UUID> receiverIds,
            String eventName,
            Object data
    ) {
        this(
                id,
                Set.copyOf(receiverIds),
                eventName,
                data
        );
    }

    public boolean canReceive(UUID receiverId) {
        return receiverIds.isEmpty()
                || receiverIds.contains(receiverId);
    }
}