package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;

@Getter
public class DiscodeitException extends RuntimeException { // 모든 예외의 기본

  private final Instant timestamp;
  private final ErrorCode errorCode;
  private final Map<String, Object> details; // 예외 발생 상황에 대한 추가정보 저장하기 위한 속성

  public DiscodeitException(ErrorCode errorCode, String message, Map<String, Object> details) {
    super(message);
    this.timestamp = Instant.now();
    this.errorCode = errorCode;
    this.details = details != null ? details : Map.of();
  }
}
