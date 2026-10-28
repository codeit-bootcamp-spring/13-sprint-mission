package com.sprint.mission.discodeit.sse;

import java.util.Set;
import java.util.UUID;
import java.time.Instant;

public record SseMessage(UUID id, Instant createdAt, Set<UUID> receiverIds,
                         String eventName, Object data) {
  public boolean visibleTo(UUID receiverId) {
    return receiverIds == null || receiverIds.contains(receiverId);
  }
}
