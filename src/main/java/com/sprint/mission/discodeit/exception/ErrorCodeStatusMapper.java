package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorCodeStatusMapper {

    public HttpStatus map(ErrorCode errorCode) {
        return switch (errorCode) {
            case USER_NOT_FOUND,
                 CHANNEL_NOT_FOUND,
                 MESSAGE_NOT_FOUND,
                 BINARY_CONTENT_NOT_FOUND,
                 READ_STATUS_NOT_FOUND,
                 USER_STATUS_NOT_FOUND -> HttpStatus.NOT_FOUND;

            case DUPLICATE_USER,
                 DUPLICATE_READ_STATUS,
                 DUPLICATE_USER_STATUS,
                 PRIVATE_CHANNEL_UPDATE,
                 INVALID_MESSAGE_CONTENT,
                 INVALID_CREDENTIALS,
                 BINARY_CONTENT_STORAGE_ERROR -> HttpStatus.BAD_REQUEST;
        };
    }
}