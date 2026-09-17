package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.UserRoleUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserRoleUpdater implements UserRoleUpdater {

    private final UserRoleManager userRoleManager;

    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public UserDto updateRole(UUID userId, UserRoleUpdateCommand command) {
        return userRoleManager.updateRole(userId, command);
    }
}
