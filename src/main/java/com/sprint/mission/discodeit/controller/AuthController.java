package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.AuthApi;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

  private final AuthService authService;

  @GetMapping(path = "csrf-token")
  @Override
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity
            .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
            .build();
  }

  @GetMapping(path = "me")
  @Override
  public ResponseEntity<UserDto> getCurrentUser(
          @AuthenticationPrincipal DiscodeitUserDetails userDetails) {
    if (userDetails == null) {
      log.debug("현재 사용자 조회 실패: 인증되지 않은 요청");
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .build();
    }

    UserDto userDto = userDetails.getUserDto();
    log.debug("현재 사용자 조회: userId={}, username={}", userDto.id(), userDto.username());
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(userDto);
  }

  @PutMapping(path = "role")
  @Override
  public ResponseEntity<UserDto> updateRole(@RequestBody @Valid RoleUpdateRequest request) {
    log.info("사용자 권한 수정 요청: {}", request);
    UserDto updatedUser = authService.updateRole(request);
    log.debug("사용자 권한 수정 응답: {}", updatedUser);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedUser);
  }
}