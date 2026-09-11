package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.security.role.Role;

import java.util.UUID;

public interface AuthService {

    UserDto roleUpdate(UUID userId, Role role);

}
