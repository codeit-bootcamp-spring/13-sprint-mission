package com.sprint.mission.discodeit.event.user;

import com.sprint.mission.discodeit.entity.Role;
import java.util.UUID;

public record RoleUpdatedEvent(UUID userId, Role previousRole, Role newRole) {
}
