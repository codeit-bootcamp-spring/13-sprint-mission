package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtRefreshResult;
import com.sprint.mission.discodeit.dto.response.UserResponse;

public interface AuthService {

    JwtRefreshResult refresh(String refreshToken);

    UserResponse updateRole(UserRoleUpdateRequest request);
}