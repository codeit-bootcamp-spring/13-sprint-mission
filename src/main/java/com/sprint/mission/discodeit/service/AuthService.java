package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.auth.LoginCommand;
import com.sprint.mission.discodeit.dto.user.UserDto;

public interface AuthService {
    UserDto login (LoginCommand command);
}
