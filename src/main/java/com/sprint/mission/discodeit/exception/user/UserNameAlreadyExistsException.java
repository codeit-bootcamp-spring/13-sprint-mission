package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;

public class UserNameAlreadyExistsException extends UserException {
    public UserNameAlreadyExistsException(String userName) {
        super(ErrorCode.DUPLICATE_USER_NAME, Map.of("userName", userName));
    }
}
