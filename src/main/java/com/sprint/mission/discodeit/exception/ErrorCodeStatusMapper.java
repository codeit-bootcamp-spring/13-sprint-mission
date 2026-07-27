package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorCodeStatusMapper {

  public HttpStatus map(ErrorCode errorCode) {
    return switch (errorCode) {
      case USER_NOT_FOUND,
           USER_STATUS_NOT_FOUND,
           CHANNEL_NOT_FOUND,
           MESSAGE_NOT_FOUND,
           READ_STATUS_NOT_FOUND,
           BINARY_CONTENT_NOT_FOUND,
           ENDPOINT_NOT_FOUND -> HttpStatus.NOT_FOUND;

      case DUPLICATE_USER,
           READ_STATUS_ALREADY_EXISTS -> HttpStatus.CONFLICT;

      case WRONG_PASSWORD -> HttpStatus.UNAUTHORIZED;

      case PRIVATE_CHANNEL_UPDATE,
           INVALID_REQUEST -> HttpStatus.BAD_REQUEST;

      case BINARY_CONTENT_UPLOAD_FAILED,
           BINARY_CONTENT_DOWNLOAD_FAILED,
           INTERNAL_SERVER_ERROR -> HttpStatus.INTERNAL_SERVER_ERROR;
    };
  }

}
