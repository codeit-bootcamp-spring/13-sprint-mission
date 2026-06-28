package com.sprint.mission.discodeit.dto.request;

import java.util.UUID;

public record UserUpdateRequest(
        UUID id,
        String userName,
        String email,
        String password,
        BinaryContentCreateRequest profile
) {
}
