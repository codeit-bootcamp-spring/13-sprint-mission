package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.auth.InvalidRefreshTokenException;
import com.sprint.mission.discodeit.security.CustomUserDetails;
import com.sprint.mission.discodeit.security.CustomUserDetailsService;
import com.sprint.mission.discodeit.security.JwtInformation;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final CustomUserDetailsService userDetailsService;

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();

    log.debug("CSRF 토큰 요청: {}", tokenValue);

    return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
  }

  @PutMapping("/role")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDto> updateRole(
      @Valid @RequestBody UserRoleUpdateRequest request
  ) {
    UserDto response = userService.updateRole(request);

    return ResponseEntity.ok(response);
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(
          value = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
          required = false
      ) String refreshToken, HttpServletResponse response
  ) {
    if (refreshToken == null
        || !jwtTokenProvider.validateRefreshToken(refreshToken)
        || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {

      throw new InvalidRefreshTokenException();
    }

    String email = jwtTokenProvider.getEmail(refreshToken);

    CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(
        email);

    UserDto userDto = userDetails.getUserDto();

    String newAccessToken = jwtTokenProvider.createAccessToken(userDto);

    String newRefreshToken = jwtTokenProvider.createRefreshToken(userDto);

    JwtInformation newJwtInformation = new JwtInformation(userDto, newAccessToken, newRefreshToken);

    jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

    ResponseCookie refreshCookie = jwtTokenProvider.createRefreshTokenCookie(newRefreshToken);

    response.addHeader(
        HttpHeaders.SET_COOKIE, refreshCookie.toString()
    );

    JwtDto responseDto = new JwtDto(userDto, newAccessToken);

    return ResponseEntity.ok(responseDto);
  }
}
