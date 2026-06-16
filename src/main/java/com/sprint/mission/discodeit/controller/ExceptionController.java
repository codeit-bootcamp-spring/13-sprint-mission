package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.DiscodeitUserException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(value = Exception.class)
    public ProblemDetail handleDefaultException(Exception e, WebRequest request) {
        // 1. 오류 결과를 담을 Map을 선언합니다. (key: 필드명, value: 메시지)
//        Map<String, String> errors = new HashMap<>();

        /*
        // BindingResult: 오류 결과 보고서
        BindingResult bindingResult = e.getBindingResult();

        // BindingResult에서 @Valid에 실패한 필드 목록을 불러옵니다.
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError error : fieldErrors) {
            String field = error.getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        }
         */
//        e.getBindingResult().getFieldErrors().forEach((error) -> {
//            errors.put(error.getField(), error.getDefaultMessage());
//        });
        log.error(e.getMessage(), e);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,"");
        pd.setTitle("예기치 못한 서버 에러");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    public ProblemDetail handleNotFoundException(Throwable e){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,"page not found");
        return pd;
    }

    @ExceptionHandler(value = DiscodeitException.class)
    public ProblemDetail handleDiscodeitException(DiscodeitUserException e, WebRequest request) {
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
