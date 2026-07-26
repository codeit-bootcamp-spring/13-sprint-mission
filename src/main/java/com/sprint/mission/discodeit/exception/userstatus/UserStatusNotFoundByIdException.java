package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class UserStatusNotFoundByIdException extends UserStatusException {
    public UserStatusNotFoundByIdException(UUID userStatusId) {
        super(ErrorCode.USER_STATUS_NOT_FOUND, Map.of("userStatusId", userStatusId));
    }
}
