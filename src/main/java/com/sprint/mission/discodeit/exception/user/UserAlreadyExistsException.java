package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;

public class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException(Map<String, Object> details) {
        super(ErrorCode.DUPLICATE_USER, details);
    }


    public static UserAlreadyExistsException withName(String username) {
        return new UserAlreadyExistsException(Map.of("username", username));
    }


    public static UserAlreadyExistsException withEmail(String email) {
        return new UserAlreadyExistsException(Map.of("email", email));
    }
}
