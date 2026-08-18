package com.sprint.mission.discodeit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DiscodeitException.class)
    public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException ex) {

        ErrorResponse errorResponse = ErrorResponse.of(
                ex.getErrorCode(),
                ex.getDetails(),
                ex.getClass().getSimpleName()
        );

        return ResponseEntity
                .status(ex.getErrorCode().getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        Map<String, Object> details = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError -> details.put(fieldError.getField(), fieldError.getDefaultMessage()));

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                details,
                ex.getClass().getSimpleName()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        Map<String, Object> details = new HashMap<>();
        details.put("parameter", ex.getParameterName());
        details.put("message", "필수 요청 파라미터가 누락되었습니다.");

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                details,
                ex.getClass().getSimpleName()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {

        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        Map<String, Object> details = Map.of();

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode,
                details,
                ex.getClass().getSimpleName()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }

}
