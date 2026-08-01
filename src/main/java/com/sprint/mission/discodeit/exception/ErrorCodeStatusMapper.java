package com.sprint.mission.discodeit.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ErrorCodeStatusMapper {

  private static final Map<ErrorCode, HttpStatus> STATUS_MAP = Map.ofEntries(
      Map.entry(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND),
      Map.entry(ErrorCode.DUPLICATE_USERNAME, HttpStatus.CONFLICT),
      Map.entry(ErrorCode.DUPLICATE_EMAIL, HttpStatus.CONFLICT),

      Map.entry(ErrorCode.CHANNEL_NOT_FOUND, HttpStatus.NOT_FOUND),
      Map.entry(ErrorCode.PRIVATE_CHANNEL_UPDATE, HttpStatus.BAD_REQUEST),

      Map.entry(ErrorCode.MESSAGE_NOT_FOUND, HttpStatus.NOT_FOUND),

      Map.entry(ErrorCode.READ_STATUS_NOT_FOUND, HttpStatus.NOT_FOUND),
      Map.entry(ErrorCode.READ_STATUS_ALREADY_EXISTS, HttpStatus.CONFLICT),

      Map.entry(ErrorCode.USER_STATUS_NOT_FOUND, HttpStatus.NOT_FOUND),
      Map.entry(ErrorCode.USER_STATUS_ALREADY_EXISTS, HttpStatus.CONFLICT),

      Map.entry(ErrorCode.BINARY_CONTENT_NOT_FOUND, HttpStatus.NOT_FOUND),

      Map.entry(ErrorCode.INVALID_CREDENTIALS, HttpStatus.UNAUTHORIZED)
  );

  public HttpStatus resolve(ErrorCode errorCode) {
    return STATUS_MAP.getOrDefault(errorCode, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}