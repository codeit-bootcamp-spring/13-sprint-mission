package com.sprint.mission.discodeit.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    //DuplicateResourceException 예외 발생했을 때 처리
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Void> handleDuplicateResourceException(DuplicateResourceException e) {
        log.warn(e.getMessage());
        return ResponseEntity.badRequest().build();
    }

    //ObjectNotFoundException 예외 발생했을 때 처리
    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<Void> handleObjectNotFoundException(ObjectNotFoundException e) {
        log.warn(e.getMessage());
        return ResponseEntity.notFound().build();
    }

    //WrongTypeException 예외 발생했을 때 처리
    @ExceptionHandler(WrongTypeException.class)
    public ResponseEntity<Void> handleWrongTypeException(WrongTypeException e) {
        log.warn(e.getMessage());
        return ResponseEntity.badRequest().build();
    }

    //FileException 예외 발생했을 때 처리
    @ExceptionHandler(FileException.class)
    public ResponseEntity<Void> handleFileException(FileException e) {
        log.warn(e.getMessage());
        return ResponseEntity.internalServerError().build();
    }

    //MethodArgumentNotValidException 예외 발생했을 때 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn(e.getMessage());
        return ResponseEntity.badRequest().build();
    }

}
