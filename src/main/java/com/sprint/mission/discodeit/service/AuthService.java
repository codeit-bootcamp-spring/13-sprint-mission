package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.input.Login;
import com.sprint.mission.discodeit.dto.output.UserOutput;

public interface AuthService {
    public UserOutput login(Login login);
}
