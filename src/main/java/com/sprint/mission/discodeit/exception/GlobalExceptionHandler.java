package com.sprint.mission.discodeit.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 직접 정의한 DiscodeitException 계열 예외 처리
     */
    @ExceptionHandler(DiscodeitException.class)
    public ResponseEntity<ErrorResponse> handleDiscodeitException(
            DiscodeitException exception
    ) {
        ErrorCode errorCode = exception.getErrorCode();

        log.warn(
                "커스텀 예외 발생: exceptionType={}, errorCode={}, details={}",
                exception.getClass().getSimpleName(),
                errorCode.getCode(),
                exception.getDetails()
        );

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .exceptionType(exception.getClass().getSimpleName())
                .details(exception.getDetails())
                .build();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * @Valid 검증 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, Object> details = new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult().getFieldErrors()) {

            details.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        log.warn("요청값 검증 실패: details={}", details);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .exceptionType(exception.getClass().getSimpleName())
                .details(details)
                .build();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * 잘못된 메서드 인자 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;

        log.warn(
                "잘못된 요청값 예외 발생: message={}",
                exception.getMessage()
        );

        String message = exception.getMessage() != null
                ? exception.getMessage()
                : errorCode.getMessage();

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(errorCode.getStatus().value())
                .code(errorCode.getCode())
                .message(message)
                .exceptionType(exception.getClass().getSimpleName())
                .details(Map.of())
                .build();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * 예상하지 못한 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception exception
    ) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        log.error(
                "처리되지 않은 예외 발생: exceptionType={}, message={}",
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                exception
        );

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .exceptionType(exception.getClass().getSimpleName())
                .details(Map.of())
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}