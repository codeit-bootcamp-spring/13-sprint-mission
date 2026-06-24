package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)   // 응답 상태 코드 = 404
    public String handleIllegalArgument(IllegalArgumentException e, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("message", e.getMessage());
        return "error"; // 공통 에러 페이지
    }

    @ExceptionHandler(Exception.class)      // 그 외 모든 예외
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)  // 500
    public String handleException(Exception e, Model model) {

        // 디버깅용 오류 확인
        e.printStackTrace();

        model.addAttribute("status", 500);
        model.addAttribute("message", e.getMessage());
        return "error";
    }

}
