package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  @PostMapping(value = "/login", consumes = "application/json")
  public ResponseEntity<UserDto> login(@RequestBody @Valid LoginRequest request) {
    UserDto response = authService.login(request);

    return ResponseEntity.ok().body(response);
  }

}
