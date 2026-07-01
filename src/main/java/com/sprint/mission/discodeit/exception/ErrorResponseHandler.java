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
        GlobalException response = new GlobalException(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "서버 내부 오류가 발생했습니다."
        );

        return ResponseEntity.badRequest().body(response);
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
}
