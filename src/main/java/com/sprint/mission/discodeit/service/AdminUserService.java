package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import com.sprint.mission.discodeit.dto.response.UserDto;

public interface AdminUserService {
    UserDto createAdmin(UserCreateCommand command);
}
