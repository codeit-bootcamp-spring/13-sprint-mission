package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.command.user.UserRoleUpdateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.service.AdminUserService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicAdminUserService implements AdminUserService {

    private final UserService userService;
    private final UserRoleManager userRoleManager;

    @Transactional
    @Override
    public UserDto createAdmin(UserCreateCommand command) {
        UserDto userDto = userService.create(command, null);

        return userRoleManager.updateRole(userDto.id(), new UserRoleUpdateCommand(Role.ADMIN));
    }
}
