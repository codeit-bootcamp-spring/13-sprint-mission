package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.auth.LoginResponse;
import com.sprint.mission.discodeit.dto.command.auth.LoginCommand;
import com.sprint.mission.discodeit.dto.user.UserResponse;

public interface AuthService {
    LoginResponse login (LoginCommand command);
}
