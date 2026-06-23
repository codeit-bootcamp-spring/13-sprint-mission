package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(value = Exception.class)
    public ProblemDetail handleDefaultException(Exception e, WebRequest request) {
        log.error(e.getMessage(), e);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,"");
        pd.setTitle("핸들링 하지 못한 서버 에러");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(value = DiscodeitException.class)
    public ProblemDetail handleDiscodeitException(DiscodeitException e, WebRequest request) {
        HttpStatus code = switch (e.getCode()) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 404 -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(code,e.getMessage());
        pd.setTitle(e.getType() + " Control error");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

}
