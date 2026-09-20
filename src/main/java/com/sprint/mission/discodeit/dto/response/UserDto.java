package com.sprint.mission.discodeit.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sprint.mission.discodeit.security.role.Role;

import java.util.UUID;

public record UserDto(
        UUID id,
        String username,
        String email,
        BinaryContentDto profile,
        Boolean online,
        Role role
) {
}
