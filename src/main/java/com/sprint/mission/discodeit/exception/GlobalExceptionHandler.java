package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e) {

    Map<String, Object> details = new HashMap<>();
    for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      details.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    log.warn("[유효성 검증 실패] details={}", details);

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        "VALIDATION_FAILED",
        "요청 값이 유효하지 않습니다.",
        details,
        e.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST.value()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }
}