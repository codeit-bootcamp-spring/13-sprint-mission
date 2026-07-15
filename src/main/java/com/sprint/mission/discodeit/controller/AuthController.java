package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  // 로그인
  @RequestMapping(
      value = "/login",
      method = RequestMethod.POST
  )
  public Object login(
      @RequestBody LoginRequest request
  ) {

    return authService.login(request);
  }
}