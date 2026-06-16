package com.sprint.mission.discodeit.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler(value = Exception.class)
    public ProblemDetail handleException(Exception e) {
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
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,e.getMessage());
        pd.setTitle("입력 검증 실패");
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("errors", "error");
        return pd;
    }

}
