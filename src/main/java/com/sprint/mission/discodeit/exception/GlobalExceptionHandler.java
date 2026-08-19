package com.sprint.mission.discodeit.exception;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    HttpStatus status = ErrorCodeStatusMapper.resolve(e.getErrorCode());
    log.warn("[예외 발생] type={}, code={}, message={}, details={}",
        e.getClass().getSimpleName(), e.getErrorCode(), e.getMessage(), e.getDetails());

    return ResponseEntity
        .status(status)
        .body(ErrorResponse.of(e, status));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e) {

    Map<String, List<String>> details = e.getBindingResult()
        .getFieldErrors()
        .stream()
        .collect(Collectors.groupingBy(
            FieldError::getField,
            Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
        ));

    Map<String, Object> detailsAsObject = new java.util.HashMap<>(details);

    log.warn("[유효성 검증 실패] details={}", details);

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        "VALIDATION_FAILED",
        "요청 값이 유효하지 않습니다.",
        detailsAsObject,
        e.getClass().getSimpleName(),
        HttpStatus.BAD_REQUEST.value()
    );

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception e) {
    log.error("[예상치 못한 예외 발생] type={}, message={}",
        e.getClass().getSimpleName(), e.getMessage(), e);

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        "INTERNAL_SERVER_ERROR",
        "서버 내부 오류가 발생했습니다.",
        Map.of(),
        e.getClass().getSimpleName(),
        HttpStatus.INTERNAL_SERVER_ERROR.value()
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }
}