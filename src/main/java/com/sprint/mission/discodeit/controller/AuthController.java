package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Auth", description = "인증 API") // 그룹 묶기
public class AuthController {

  private final AuthService authService;

  // 권한 관리
  // 사용자 로그인 가능
  @Operation(summary = "로그인") // 엔드포인트 설명
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
      @ApiResponse(responseCode = "400", description = "비밀번호가 일치하지 않음")
  })
  @RequestMapping(value = "/login", method = RequestMethod.POST) // 비밀번호 같은 민감한 정보는 숨겨서 전달한다
  public ResponseEntity<User> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(authService.login(request));
  }
}
