package com.sprint.mission.discodeit.exception;

import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DiscodeitException.class)
    public ResponseEntity<ErrorResponse> handleDiscodeitException(DiscodeitException e) {
        HttpStatus status = resolveStatus(e.getErrorCode());
        log.warn("[{}] {}", e.getErrorCode(), e.getMessage());

        ErrorResponse response = new ErrorResponse(
                e.getTimestamp(),
                e.getErrorCode().name(),
                e.getMessage(),
                e.getDetails(),
                e.getClass().getSimpleName(),
                status.value()
        );

        return ResponseEntity.status(status).body(response);
    }

    private HttpStatus resolveStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case USER_NOT_FOUND, CHANNEL_NOT_FOUND, MESSAGE_NOT_FOUND,
                 READ_STATUS_NOT_FOUND, USER_STATUS_NOT_FOUND, BINARY_CONTENT_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATE_USER, READ_STATUS_ALREADY_EXISTS, USER_STATUS_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_PASSWORD -> HttpStatus.UNAUTHORIZED;
            case PRIVATE_CHANNEL_UPDATE -> HttpStatus.BAD_REQUEST;
        };
    }
}
