package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.LoginRequest;
import com.sprint.mission.discodeit.dto.output.UserOutput;

public interface AuthService {
    UserOutput login(LoginRequest loginRequest);
}
