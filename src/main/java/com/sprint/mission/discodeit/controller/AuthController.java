package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.JwtRefreshResult;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.security.JwtRefreshService;
import com.sprint.mission.discodeit.security.RefreshTokenCookieProvider;
import com.sprint.mission.discodeit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;
  private final JwtRefreshService jwtRefreshService;
  private final RefreshTokenCookieProvider refreshTokenCookieProvider;

  @Operation(summary = "CSRF 토큰 발급")
  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();

    log.debug("CSRF 토큰 요청: {}", tokenValue);

    return ResponseEntity
        .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
        .build();
  }

  @Operation(summary = "사용자 권한 수정")
  @PutMapping("/role")
  public ResponseEntity<UserDto> updateRole(
      @Valid @RequestBody UserRoleUpdateRequest request
  ) {
    UserDto response = userService.updateRole(
        request.userId(),
        request.newRole()
    );

    return ResponseEntity.ok(response);
  }

  @Operation(summary = "액세스 토큰 재발급")
  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(
          name = RefreshTokenCookieProvider.COOKIE_NAME,
          required = false
      ) String refreshToken,
      HttpServletResponse response
  ) {
    JwtRefreshResult result = jwtRefreshService.refresh(refreshToken);

    refreshTokenCookieProvider.addCookie(response, result.refreshToken());

    return ResponseEntity.ok(result.jwtDto());
  }
}