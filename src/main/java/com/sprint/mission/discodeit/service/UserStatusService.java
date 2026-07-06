package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.request.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.UserStatus;

import java.util.UUID;

public interface UserStatusService {
    UserStatus updateByUserId(UUID id, UserStatusUpdateRequest usur);
}
