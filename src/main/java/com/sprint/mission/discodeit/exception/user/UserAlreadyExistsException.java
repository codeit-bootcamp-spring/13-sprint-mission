package com.sprint.mission.discodeit.exception.user;

import com.sprint.mission.discodeit.exception.ErrorCode;

import java.util.Map;

public class UserAlreadyExistsException extends UserException {

    public UserAlreadyExistsException(
            ErrorCode errorCode,
            String field,
            String value
    ) {
        super(
                errorCode,
                Map.of(
                        "field", field,
                        "value", value
                )
        );
    }
}