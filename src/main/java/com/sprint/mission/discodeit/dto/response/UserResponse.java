package com.sprint.mission.discodeit.dto.response;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String userName,
        String email,
        boolean isOnline
) {
}
