package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;

public interface AuthService {
    LoginResponse login(LoginCommand command);
}
