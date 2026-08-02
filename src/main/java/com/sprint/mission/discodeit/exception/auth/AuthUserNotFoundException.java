package com.sprint.mission.discodeit.exception.auth;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;

public class AuthUserNotFoundException extends AuthException{

    public AuthUserNotFoundException(String username) {
        super(ErrorCode.USER_NOT_FOUND, Map.of("username", username));
    }
}
