package com.sprint.mission.discodeit.dto.command.user;

import com.sprint.mission.discodeit.entity.Role;
import jakarta.validation.constraints.NotNull;

public record UserRoleUpdateCommand(
        @NotNull
        Role newRole
) {
}
