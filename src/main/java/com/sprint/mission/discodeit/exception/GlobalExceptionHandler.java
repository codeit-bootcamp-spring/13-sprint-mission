package com.sprint.mission.discodeit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateResourceException.class)
    public void handleDuplicateResourceException(DuplicateResourceException e) {
        log.error(e.getMessage());
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public void handleObjectNotFoundException(ObjectNotFoundException e) {
        log.error(e.getMessage());
    }

    @ExceptionHandler(WrongTypeException.class)
    public void handleWrongTypeException(WrongTypeException e) {
        log.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public void handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
    }

}
