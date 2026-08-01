package com.sprint.mission.discodeit.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  private final ErrorCodeStatusMapper errorCodeStatusMapper;

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    HttpStatus status = errorCodeStatusMapper.resolve(e.getErrorCode());
    log.warn("[예외 발생] type={}, code={}, message={}, details={}",
        e.getClass().getSimpleName(), e.getErrorCode(), e.getMessage(), e.getDetails());

    return ResponseEntity
        .status(status)
        .body(ErrorResponse.of(e, status));
  }
}