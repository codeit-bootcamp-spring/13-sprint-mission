package com.sprint.mission.discodeit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    ErrorCode errorCode = e.getErrorCode();
    // WARN: 커스텀 예외는 시스템 오류가 아니라 비즈니스 로직 실패이므로 warn 레벨
    log.warn("커스텀 예외 발생: errorCode={}, message={}, details={}",
            errorCode.name(), errorCode.getMessage(), e.getDetails());
    return ResponseEntity
            .status(errorCode.getStatus())
            .body(ErrorResponse.of(e));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    // ERROR: 예상치 못한 예외는 심각한 문제일 수 있으므로 error 레벨
    log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);
    return ResponseEntity
            .internalServerError()
            .body(new ErrorResponse(
                    java.time.Instant.now(),
                    500,
                    e.getClass().getSimpleName(),
                    "INTERNAL_SERVER_ERROR",
                    "서버 내부 오류가 발생했습니다.",
                    null
            ));
  }

  // @Valid 검증 실패 시 Spring이 자동으로 던지는 예외
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
          MethodArgumentNotValidException e) {

    Map<String, Object> details = new HashMap<>();
    for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
      details.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    log.warn("유효성 검사 실패: {}", details);

    return ResponseEntity
            .badRequest()  // 400
            .body(new ErrorResponse(
                    java.time.Instant.now(),
                    400,
                    "MethodArgumentNotValidException",
                    "VALIDATION_FAILED",
                    "입력값이 올바르지 않습니다.",
                    details
            ));
  }

  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(
          HttpMediaTypeNotSupportedException e) {
    log.warn("지원하지 않는 미디어 타입: {}", e.getContentType());
    return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(new ErrorResponse(
                    Instant.now(),
                    415,
                    "HttpMediaTypeNotSupportedException",
                    "UNSUPPORTED_MEDIA_TYPE",
                    "지원하지 않는 미디어 타입입니다.",
                    null
            ));
  }
}