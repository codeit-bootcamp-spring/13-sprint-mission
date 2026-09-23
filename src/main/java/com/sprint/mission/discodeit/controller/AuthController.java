package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.user.RoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
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

  private final AuthService authService;
  private final UserService userService;

  @GetMapping("/csrf-token")
  public ResponseEntity<Object> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);

    return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build(); //203 VOID
  }

  @GetMapping("/me")
  public UserResponse me(@AuthenticationPrincipal DiscodeitUserDetails principal) {
    return userService.findUserById(principal.getUserDto().id());
  }

  @PostMapping("/role")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> changeRole(@Valid @RequestBody RoleUpdateRequest request) {
    return ResponseEntity.ok().body(userService.changeRole(request.userid(), request.newRole()));

  }
}
