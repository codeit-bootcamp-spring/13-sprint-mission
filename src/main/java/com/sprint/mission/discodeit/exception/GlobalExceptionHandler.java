package com.sprint.mission.discodeit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //DuplicateResourceException 예외 발생했을 때 처리
    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicateResourceException(DuplicateResourceException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("리소스 중복 예외 발생");
        pd.setProperty("timestamp", Instant.now());

        log.warn(e.getMessage());

        return pd;
    }

    //ObjectNotFoundException 예외 발생했을 때 처리
    @ExceptionHandler(ObjectNotFoundException.class)
    public ProblemDetail handleObjectNotFoundException(ObjectNotFoundException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        pd.setTitle("데이터 검색 실패 예외 발생");
        pd.setProperty("timestamp", Instant.now());

        log.warn(e.getMessage());

        return pd;
    }

    //WrongTypeException 예외 발생했을 때 처리
    @ExceptionHandler(WrongTypeException.class)
    public ProblemDetail handleWrongTypeException(WrongTypeException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("Type 미스매치 예외 발생");
        pd.setProperty("timestamp", Instant.now());

        log.warn(e.getMessage());

        return pd;
    }

    //FileException 예외 발생했을 때 처리
    @ExceptionHandler(FileException.class)
    public ProblemDetail handleFileException(FileException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        pd.setTitle("파일 관련 예외 발생");
        pd.setProperty("timestamp", Instant.now());

        log.warn(e.getMessage());

        return pd;
    }

    //MethodArgumentNotValidException 예외 발생했을 때 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        pd.setTitle("입력값 검증 예외 발생");
        pd.setProperty("timestamp", Instant.now());

        log.warn(e.getMessage());

        return pd;
    }

}
