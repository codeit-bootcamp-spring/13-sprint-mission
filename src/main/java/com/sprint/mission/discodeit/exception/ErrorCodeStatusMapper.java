package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorCodeStatusMapper {

    public HttpStatus toHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case USER_NOT_FOUND,
                 CHANNEL_NOT_FOUND,
                 MESSAGE_NOT_FOUND,
                 BINARY_CONTENT_NOT_FOUND,
                 READ_STATUS_NOT_FOUND,
                 USER_STATUS_NOT_FOUND,
                 RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case USER_ALREADY_EXISTS,
                 READ_STATUS_ALREADY_EXISTS,
                 USER_STATUS_ALREADY_EXISTS -> HttpStatus.CONFLICT;

            case INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case PRIVATE_CHANNEL_UPDATE_NOT_ALLOWED,
                 VALIDATION_FAILED -> HttpStatus.BAD_REQUEST;
            case INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
