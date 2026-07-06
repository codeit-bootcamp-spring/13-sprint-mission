package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.UUID;

public record UserSatusDto(
        UUID id
        , UUID userId
        , Instant lastActiveAt
) {
}
