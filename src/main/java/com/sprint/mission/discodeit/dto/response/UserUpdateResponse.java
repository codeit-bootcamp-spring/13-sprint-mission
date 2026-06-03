package com.sprint.mission.discodeit.dto.response;

import java.util.UUID;

public record UserUpdateResponse(
        UUID userId,
        String name,
        String email,
        UUID profileId
) {
}
