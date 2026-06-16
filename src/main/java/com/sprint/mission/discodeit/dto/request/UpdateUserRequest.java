package com.sprint.mission.discodeit.dto.request;

import java.util.UUID;

public record UpdateUserRequest(
        UUID id,
        String username,
        String email,
        String password
) {
}
