package com.sprint.mission.discodeit.dto.readstatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record ReadStatusUpdateRequest(
        UUID id,

        @JsonProperty("newLastReadAt")
        @NotNull
        Instant lastReadAt
) {
}
