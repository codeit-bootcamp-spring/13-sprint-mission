package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.dto.auth.TokenPair;
import com.sprint.mission.discodeit.dto.user.RoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.exception.auth.InvalidRefreshTokenException;
import com.sprint.mission.discodeit.security.JwtInformation;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.security.RefreshTokenCookieProvider;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenCookieProvider refreshTokenCookieProvider;
  private final JwtRegistry jwtRegistry;

  @GetMapping("/csrf-token")
  public ResponseEntity<Object> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);

    return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build(); //203 VOID
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(name = RefreshTokenCookieProvider.COOKIE_NAME, required = false)
      String refreshToken,
      HttpServletRequest request,
      HttpServletResponse response) {
    if (!StringUtils.hasText(refreshToken)) {
      throw new InvalidRefreshTokenException();
    }

    try {
      if (!jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
        throw new InvalidRefreshTokenException();
      }

      TokenPair tokens = jwtTokenProvider.rotateTokens(refreshToken);
      UserResponse user = userService.findUserById(jwtTokenProvider.getUserId(refreshToken));
      boolean rotated = jwtRegistry.rotateJwtInformation(
          refreshToken,
          new JwtInformation(user, tokens.accessToken(), tokens.refreshToken()));
      if (!rotated) {
        throw new InvalidRefreshTokenException();
      }
      response.addCookie(
          refreshTokenCookieProvider.create(tokens.refreshToken(), request.isSecure()));
      return ResponseEntity.ok(new JwtDto(user, tokens.accessToken()));
    } catch (InvalidRefreshTokenException exception) {
      throw exception;
    } catch (IllegalArgumentException exception) {
      throw new InvalidRefreshTokenException();
    }
  }

  @PostMapping("/role")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> changeRole(@Valid @RequestBody RoleUpdateRequest request) {
    return ResponseEntity.ok().body(userService.changeRole(request.userId(), request.newRole()));

  }
}
