package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public ResponseEntity<UserDto> login(@RequestBody @Valid LoginRequest loginRequest) {
    UserDto useLogin = authService.login(loginRequest.username(),
        loginRequest.password());
    return ResponseEntity.status(HttpStatus.OK).body(useLogin);
  }                                                //└>RequestMethod.POST 지만
  // 새 리소스가 생긴게 아니라 200 OK 상태 메세지
}
