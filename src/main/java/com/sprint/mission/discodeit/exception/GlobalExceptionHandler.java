package com.sprint.mission.discodeit.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(BinaryContentNotFoundException.class)
  public ProblemDetail handleBinaryContentNotFoundException(BinaryContentNotFoundException e) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    pd.setTitle("Resource Not Found");
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }

  @ExceptionHandler(ChannelNotFoundException.class)
  public ProblemDetail handleChannelNotFoundException(ChannelNotFoundException e) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    pd.setTitle("Resource Not Found");
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }

  @ExceptionHandler(PrivateChannelUnmodifiableException.class)
  public ProblemDetail handlePrivateChannelUnmodifiableException(
      PrivateChannelUnmodifiableException e) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    pd.setTitle("Invalid Request");
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }

  @ExceptionHandler(UserNotFoundException.class)
  public ProblemDetail handleUserNotFoundException(UserNotFoundException e) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    pd.setTitle("Resource Not Found");
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }

  @ExceptionHandler(ReadStatusAlreadyExistsException.class)
  public ProblemDetail handleReadStatusAlreadyExistsException(ReadStatusAlreadyExistsException e) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    pd.setTitle("Invalid Request");
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }

  @ExceptionHandler(ReadStatusNotFoundException.class)
  public ProblemDetail handleReadStatusNotFoundException(ReadStatusNotFoundException e) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    pd.setTitle("Resource Not Found");
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }

  @ExceptionHandler(DuplicateUserException.class)
  public ProblemDetail handleDuplicateUserException(DuplicateUserException e) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    pd.setTitle("Invalid Request");
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }

  @ExceptionHandler(UserStatusNotFoundException.class)
  public ProblemDetail handleUserStatusNotFoundException(UserStatusNotFoundException e) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    pd.setTitle("Resource Not Found");
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
    Map<String, String> errors = new HashMap<>();
    e.getBindingResult().getFieldErrors().forEach(error -> {
      errors.put(error.getField(), error.getDefaultMessage());
    });

    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
        "요청 본문의 일부 필드가 유효하지 않습니다.");
    pd.setTitle("Invalid Request");
    pd.setProperty("errors", errors);
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleNotReadable(HttpMessageNotReadableException e) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
        "요청 본문(JSON)을 읽을 수 없습니다. 형식이나 값을 확인하세요.");
    pd.setTitle("Invalid Request");
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleException(Exception e) {
    log.error("예상치 못한 서버 오류", e);
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
        "서버에서 문제가 발생했습니다. 잠시 후 다시 시도해 주세요.");
    pd.setTitle("Internal Server Error");
    pd.setProperty("timestamp", Instant.now());
    return pd;
  }

}
