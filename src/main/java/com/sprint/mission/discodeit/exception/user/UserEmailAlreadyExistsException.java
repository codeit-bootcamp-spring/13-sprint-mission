package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;

public class UserEmailAlreadyExistsException extends UserException {
    public UserEmailAlreadyExistsException(String email) {
        super(ErrorCode.DUPLICATE_USER_EMAIL, Map.of("email", email));
    }
}
