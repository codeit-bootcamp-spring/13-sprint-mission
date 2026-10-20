package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 API") // 그룹 묶기
public class AuthController {

  // CSRF 토큰을 발급하는 API
  @Operation(summary = "CSRF 토큰 발급")
  @ApiResponse(
      responseCode = "203",
      description = "CSRF 토큰 발급 성공"
  )
  @GetMapping("csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    // CsrfToken 파라미터를 메서드 인자로 선언하면 HandlerMethodArgumentResolver를 통해 자동으로 주입
    // GET 요청에는 CSRF 인증이 이루어지지 않기 때문에 토큰이 초기화되지 않은 상태로 들어온다. 따라서 메소드에서 명시적으로 토큰을 호출
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity
        .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION) // 203 응답
        .build();
  }

  // 세션ID 통해 사용자의 기본 정보 가져오는 API
  @Operation(summary = "사용자 기본 정보 조회")
  @ApiResponse(
      responseCode = "200",
      description = "사용자의 기본 정보 조회 성공"
  )
  @GetMapping("/me")
  public ResponseEntity<UserDto> getMe(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  ) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(userDetails.getUserDto());
  }
}
