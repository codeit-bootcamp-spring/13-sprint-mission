package com.sprint.mission.discodeit.dto.userstatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record UserStatusUpdateRequest(
        UUID id,

        @JsonProperty("newLastActiveAt")
        @NotNull
        Instant lastSeenAt
) {
}
