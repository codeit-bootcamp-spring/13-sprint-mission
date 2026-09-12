package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;

public interface AuthService {
    UserDto updateRole(UserRoleUpdateRequest request);
}
