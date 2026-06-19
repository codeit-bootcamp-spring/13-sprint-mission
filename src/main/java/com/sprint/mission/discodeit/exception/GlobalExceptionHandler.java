package com.sprint.mission.discodeit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice//모든 컨트롤러의 예외를 잡아서 json 으로 응답
//-> @ControllerAdvice + @ResponseBody
//@ControllerAdvice(빈 등록, 모든 컨트롤러 예외를 서치)
// @ResponseBody(메서드가 반환하는 값을 json으로 변환 후 HTTP 응답의 body에 담는다)
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)// → HTTP 404로 응답
    public ResponseEntity<String> handleNotFoundException(NoSuchElementException e) {
        return ResponseEntity//HTTP 응답을 직접 조립하는 객체.
                .status(HttpStatus.NOT_FOUND)//HTTP 상태코드 404
                //└> enum(열거형) 객체(Spring이 만듬)
                .body(e.getMessage());//e.getMessage()는 서비스 로직에서 온다.
    }


    @ExceptionHandler(IllegalArgumentException.class)// → HTTP 400으로 응답
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)//BAD_REQUEST -> 400
                .body(e.getMessage());
    }

}
