package com.sprint.mission.discodeit.dto.readstatus;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusUpdateRequest(
        UUID id,

        @JsonProperty("newLastReadAt")
        Instant lastReadAt
) {
}
