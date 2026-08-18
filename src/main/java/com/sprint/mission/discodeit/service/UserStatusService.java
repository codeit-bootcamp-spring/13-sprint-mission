package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.user.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserStatusDto;

import java.util.UUID;

public interface UserStatusService {
    UserStatusDto updateByUserId(UUID id, UserStatusUpdateRequest usur);
}
