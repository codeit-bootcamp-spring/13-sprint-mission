package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.dto.error.ErrorResponse;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException e) {
    ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;

    List<Map<String, Object>> fieldErrors = e.getBindingResult().getFieldErrors().stream()
        .map(fieldError -> {
          Map<String, Object> error = new LinkedHashMap<>();
          error.put("field", fieldError.getField());
          error.put("message", fieldError.getDefaultMessage());
          return error;
        })
        .toList();

    List<String> globalErrors = e.getBindingResult().getGlobalErrors().stream()
        .map(error -> error.getDefaultMessage())
        .toList();

    Map<String, Object> details = new LinkedHashMap<>();
    details.put("fieldErrors", fieldErrors);
    if (!globalErrors.isEmpty()) {
      details.put("globalErrors", globalErrors);
    }

    log.warn("요청 데이터 검증 실패: errors={}", details);

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        errorCode.name(),
        errorCode.getMessage(),
        details,
        e.getClass().getSimpleName(),
        errorCode.getStatus().value()
    );

    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("도메인 예외 발생: code={}, details={}", errorCode, e.getDetails());

    ErrorResponse response = new ErrorResponse(
        e.getTimeStamp(),
        errorCode.name(),
        e.getMessage(),
        e.getDetails(),
        e.getClass().getSimpleName(),
        errorCode.getStatus().value()
    );

    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleBadRequest(IllegalArgumentException e) {
    log.warn("잘못된 요청: {}", e.getMessage());
    return buildProblemDetail(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ProblemDetail handleIllegalState(IllegalStateException e) {
    log.warn("잘못된 상태의 요청: {}", e.getMessage());
    return buildProblemDetail(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ProblemDetail handleNotFound(NoSuchElementException e) {
    log.warn("자원을 찾을 수 없음: {}", e.getMessage());
    return buildProblemDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception e) {
    log.error("서버 오류", e);
    return buildProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR,
        ErrorCode.INTERNAL_ERROR.getMessage());
  }

  private ProblemDetail buildProblemDetail(HttpStatus status, String detail) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
    problemDetail.setProperty("timestamp", Instant.now());
    return problemDetail;
  }
}
