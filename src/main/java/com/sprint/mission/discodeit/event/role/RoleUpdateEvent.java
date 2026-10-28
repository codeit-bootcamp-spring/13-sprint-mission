package com.sprint.mission.discodeit.event.role;

import com.sprint.mission.discodeit.security.role.Role;

import java.util.UUID;

public record RoleUpdateEvent(
        UUID userId,
        Role before,
        Role after
) {
}
