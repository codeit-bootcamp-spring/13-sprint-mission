package com.sprint.mission.discodeit.realtime;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record RealtimeEvent(UUID id, Instant createdAt, String destination,
                            Set<UUID> receiverIds, String eventName, JsonNode data) { }
