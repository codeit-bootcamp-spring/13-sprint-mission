package com.sprint.mission.discodeit.dto;

import java.util.Set;
import java.util.UUID;

public record SseMessage(
    UUID id,
    Set<UUID> receiverIds,
    String eventName,
    Object data,
    boolean broadcast
) {

  public SseMessage {
    receiverIds = Set.copyOf(receiverIds);
  }

  public boolean isVisibleTo(UUID receiverId) {
    return broadcast || receiverIds.contains(receiverId);
  }
}