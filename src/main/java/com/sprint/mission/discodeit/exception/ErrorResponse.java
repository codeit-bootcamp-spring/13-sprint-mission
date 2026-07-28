package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

  private final Instant timestamp;
  private final String code;
  private final String message;
  private final Map<String, Object> details;
  private final String exceptionType; // 발생한 예외의 클래스 이름
  private final int status; // HTTP 상태코드

  public ErrorResponse(String code, String message, Map<String, Object> details,
      String exceptionType, int status) {
    this.timestamp = Instant.now();
    this.code = code;
    this.message = message;
    this.details = details != null ? details : Map.of();
    this.exceptionType = exceptionType;
    this.status = status;
  }
}
