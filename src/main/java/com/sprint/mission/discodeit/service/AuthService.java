package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.UserDto;

public interface AuthService {
    UserDto login(String username, String password);
}
