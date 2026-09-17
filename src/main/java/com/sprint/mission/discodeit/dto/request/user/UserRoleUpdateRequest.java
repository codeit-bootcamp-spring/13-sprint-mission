package com.sprint.mission.discodeit.dto.request.user;

import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.entity.Role;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UserRoleUpdateRequest(
        @NotNull
        UUID userId,
        @NotNull
        Role newRole
) {

    public UserRoleUpdateCommand toCommand() {
        return new UserRoleUpdateCommand(newRole);
    }
}
