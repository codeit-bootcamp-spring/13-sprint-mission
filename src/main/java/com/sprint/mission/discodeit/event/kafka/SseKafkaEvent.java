package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;

public record SseKafkaEvent(
        List<UUID> receiverIds,
        String eventName,
        JsonNode data
) {
}
