package com.sprint.mission.discodeit.Gloal;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(
      NoSuchElementException e) {
    Map<String, Object> body = new HashMap<>();

    body.put("status", 404);
    body.put("error", "Not Found");
    body.put("message", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);

  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handLeIllegalState(IllegalStateException e) {
    Map<String, Object> body = new HashMap<>();

    body.put("status", 400); //body.put("status", 409);
    body.put("error", "Bad Request"); //body.put("error", "Conflict");
    body.put("message", e.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(IllegalAccessException.class)
  public ResponseEntity<Map<String, Object>> handLeIllegalState(IllegalAccessException e) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", 403);
    body.put("error", "Forbidden");
    body.put("message", e.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handLeIllegalState(Exception e) {
    Map<String, Object> body = new HashMap<>();
    body.put("status", 500);
    body.put("error", "Internal Server Error");
    body.put("message", e.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}