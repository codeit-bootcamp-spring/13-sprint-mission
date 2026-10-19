package com.sprint.mission.discodeit.event;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.UUID;

public record SseEvent(
    UUID id,
    UUID receiverId,
    String name,
    JsonNode data
) {

}
