package com.sprint.mission.discodeit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(DiscodeitException.class)
  public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
    log.error("발생한 예외 클래스명: {}, 예외 메시지: {}", e.getClass().getSimpleName(), e.getMessage());
    return ResponseEntity
        .status(e.getErrorCode().getStatus())
        .body(
            ErrorResponse.builder()
                .code(e.getErrorCode().name())
                .message(e.getMessage())
                .details(e.getDetails())
                .exceptionType(e.getClass().getSimpleName())
                .status(e.getErrorCode().getStatus().value())
                .build()
        );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
    Map<String, Object> errors = new HashMap<>(); // 오류 결과 담을 Map, key는 필드명, value는 에러 메시지

    // 최대한 변수 선언 없이 method chaining 이용해 호출
    e.getBindingResult().getFieldErrors().forEach(error -> {
      errors.put(error.getField(), error.getDefaultMessage());
    });
    log.warn("유효성 검사 에러: {}", errors);
    return ResponseEntity
        .status(ErrorCode.PARAM_ERROR.getStatus())
        .body(
            ErrorResponse.builder()
                .code(ErrorCode.PARAM_ERROR.name())
                .message(ErrorCode.PARAM_ERROR.getMessage())
                .details(errors)
                .exceptionType(e.getClass().getSimpleName())
                .status(ErrorCode.PARAM_ERROR.getStatus().value())
                .build()
        );
  }

  // 400 - JSON 자체가 깨졌거나 enum에 없는 값 등, 요청 본문을 읽지 못할 때
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException e) {
    log.warn("요청 본문을 읽을 수 없음: {}", e.getMessage());
    return ResponseEntity
        .status(ErrorCode.MESSAGE_CONVERTER_ERROR.getStatus())
        .body(
            ErrorResponse.builder()
                .code(ErrorCode.MESSAGE_CONVERTER_ERROR.name())
                .message(ErrorCode.MESSAGE_CONVERTER_ERROR.getMessage())
                .details(Map.of())
                .exceptionType(e.getClass().getSimpleName())
                .status(ErrorCode.MESSAGE_CONVERTER_ERROR.getStatus().value())
                .build()
        );
  }

  // 500 - 그 밖의 예상치 못 한 오류, 원본 메시지는 로그에만 주고 클라이언트에게는 안전한 문구를 준다
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("예상치 못한 서버 오류", e);
    return ResponseEntity
        .status(ErrorCode.UNKNOWN_ERROR.getStatus())
        .body(
            ErrorResponse.builder()
                .code(ErrorCode.UNKNOWN_ERROR.name())
                .message(ErrorCode.UNKNOWN_ERROR.getMessage())
                .details(Map.of())
                .exceptionType(e.getClass().getSimpleName())
                .status(ErrorCode.UNKNOWN_ERROR.getStatus().value())
                .build()
        );
  }
}
