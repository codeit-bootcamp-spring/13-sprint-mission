package com.sprint.mission.discodeit.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ErrorResponseHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalException> handleIllegalArgumentException(
            IllegalArgumentException e
    ) {
        HttpStatus status = isNotFoundException(e)
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;

        GlobalException response = new GlobalException(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                e.getMessage()
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalException> handleException(
            Exception e
    ) {
        GlobalException response = new GlobalException(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "서버 내부 오류가 발생했습니다."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private boolean isNotFoundException(Exception e) {
        String message = e.getMessage();

        if (message == null) {
            return false;
        }

        return message.contains("존재하지")
                || message.toLowerCase().contains("not found");
    }
}
