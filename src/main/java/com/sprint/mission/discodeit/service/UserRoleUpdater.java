package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;

import java.util.UUID;

public interface UserRoleUpdater {
    UserDto updateRole(UUID userId, UserRoleUpdateCommand command);
}
