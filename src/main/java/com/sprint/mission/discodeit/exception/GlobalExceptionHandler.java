package com.sprint.mission.discodeit.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DiscodeitException.class)
    public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
        log.warn("예외 발생 - code: {}, message: {}, details: {}",
                e.getErrorCode().name(), e.getMessage(), e.getDetails());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {

        Map<String, Object> details = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(error -> details.put(error.getField(), error.getDefaultMessage()));
        log.warn("유효성 검증 실패 - details: {}", details);

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                "VALIDATION_FAILED",
                "입력값이 올바르지 않습니다.",
                details,
                e.getClass().getSimpleName(),
                400
        );
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUpload(MaxUploadSizeExceededException e){
        log.warn("파일 크기 초과");
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                ErrorCode.INVALID_FILE_SIZE.name(),
                ErrorCode.INVALID_FILE_SIZE.getMessage(),
                Map.of(),
                e.getClass().getSimpleName(),
                ErrorCode.INVALID_FILE_SIZE.getStatus().value()
        );
        return ResponseEntity.status(ErrorCode.INVALID_FILE_SIZE.getStatus()).body(response);
    }




    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("처리되지 않은 예외", e);
        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                "INTERNAL_SERVER_ERROR",
                "서버 오류가 발생했습니다.",
                Map.of(),
                e.getClass().getSimpleName(),
                500
        );
        return ResponseEntity.status(500).body(response);
    }
}
