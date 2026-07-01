package com.sprint.mission.discodeit.dto.userstatus;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record UserStatusUpdateRequest(
        UUID id,

        @JsonProperty("newLastActiveAt")
        Instant lastSeenAt
) {
}
