package com.sprint.mission.discodeit.exception.userstatus;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;
import java.util.UUID;

public class UserStatusAlreadyExistException extends UserStatusException {

    public UserStatusAlreadyExistException(UUID userId) {
        super(ErrorCode.USER_STATUS_ALREADY_EXISTS, Map.of("userId", userId));
    }
}