package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.LoginInput;
import com.sprint.mission.discodeit.dto.output.UserOutput;

public interface AuthService {
    UserOutput login(LoginInput loginInput);
}
