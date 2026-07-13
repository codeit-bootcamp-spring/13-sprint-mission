package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.LoginRequest;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor //final이 선언된 필드를 대상으로 생성자를 자동 생성하는 Lombok 어노테이션
@RestController
public class AuthController implements AuthApi {

  private final AuthService authService;

  //"api/auth/Login" 요청을 처리하는 메서드
  @Override
  public ResponseEntity<UserDto> login(@RequestBody LoginRequest loginRequest) {
    //전달받은 로그인 요청(LoginRequest)을 AuthService로 전달하여 로그인 인증을 수행함. 인증이 성공하면 해당 사용자 객체를 반환받음.
    UserDto user = authService.login(loginRequest);
    //HTTP 응답(Request)을 생성하고 상태코드는 200(OK)으로 설정하고 로그인에 성공한 사용자 정보를 Response Body에 담아 클라이언트에게 반환
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(user);
  }
}
