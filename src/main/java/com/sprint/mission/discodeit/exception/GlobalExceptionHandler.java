package com.sprint.mission.discodeit.exception;


import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // 모든 커스텀 예외 처리
  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    ErrorCode errorCode = e.getErrorCode();
    HttpStatus status = errorCode.getStatus();

    log.warn("비즈니스 예외 발생: code={}, type={}, details={}",
        errorCode.getCode(),
        e.getClass().getSimpleName(),
        e.getDetails()
    );

    ErrorResponse response = new ErrorResponse(
        e.getTimestamp(),
        errorCode.getCode(),
        e.getMessage(),
        e.getDetails(),
        status.value(),
        e.getClass().getSimpleName()
    );

    return ResponseEntity
        .status(status)
        .body(response);
  }

  // valid 검증 실패 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
    Map<String, Object> details = new HashMap<>();

    e.getBindingResult()
        .getFieldErrors()
        .forEach(error -> details.put(error.getField(), error.getDefaultMessage()));

    ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        errorCode.getCode(),
        errorCode.getMessage(),
        details,
        errorCode.getStatus().value(),
        e.getClass().getSimpleName()
    );

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(response);
  }

  // json 파싱 실패 처리
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
    ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        errorCode.getCode(),
        "요청 본문을 읽을 수 없습니다. JSON 형식과 값을 확인해 주세요.",
        Map.of(),
        errorCode.getStatus().value(),
        e.getClass().getSimpleName()
    );

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(response);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResourceFound(
      NoResourceFoundException e
  ) {
    ErrorCode errorCode = ErrorCode.ENDPOINT_NOT_FOUND;

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        errorCode.getCode(),
        errorCode.getMessage(),
        Map.of(),
        errorCode.getStatus().value(),
        e.getClass().getSimpleName()
    );

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(response);
  }

  // 예상하지 못한 서버 오류 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("예상하지 못한 서버 오류 발생", e);

    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        errorCode.getCode(),
        errorCode.getMessage(),
        Map.of(),
        errorCode.getStatus().value(),
        e.getClass().getSimpleName()
    );

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(response);
  }
}
