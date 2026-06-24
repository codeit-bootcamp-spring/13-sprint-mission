package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}
