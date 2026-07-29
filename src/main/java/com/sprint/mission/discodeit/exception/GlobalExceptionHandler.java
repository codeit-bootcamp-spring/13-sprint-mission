package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ErrorCodeStatusMapper errorCodeStatusMapper;

    @ExceptionHandler(DiscodeitException.class)
    public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException exception) {
        HttpStatus status = errorCodeStatusMapper.toHttpStatus(exception.getErrorCode());
        log.warn("요청 처리 실패: code={}, details={}",
                exception.getErrorCode().name(), exception.getDetails());

        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                status.value(),
                exception.getErrorCode().name(),
                exception.getMessage(),
                exception.getClass().getSimpleName(),
                exception.getDetails()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception exception) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("예상하지 못한 오류가 발생했습니다.", exception);

        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now(),
                status.value(),
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                exception.getClass().getSimpleName(),
                Map.of()
        ));
    }
}