package com.sprint.mission.discodeit.dto.projection;

import com.sprint.mission.discodeit.security.role.Role;

import java.util.UUID;

public record UserProjection(
        // user info
        UUID id,
        String username,
        String email,
        String password,
        Role role,

        // binaryContent info => user profile
        UUID profileId,
        String fileName,
        Long size,
        String contentType
) {
}
