package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.swagger.AuthApi;
import com.sprint.mission.discodeit.dto.request.user.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.JwtDtoWithRefresh;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.service.TokenService;
import com.sprint.mission.discodeit.service.UserRoleUpdater;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController implements AuthApi {
    private final UserRoleUpdater userRoleUpdater;
    private final TokenService tokenService;

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        String tokenValue = csrfToken.getToken();
        log.debug("CSRF 토큰 요청: {}", tokenValue);

        return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
    }

    @PutMapping("/role")
    @Override
    public ResponseEntity<UserDto> updateRole(@Valid @RequestBody UserRoleUpdateRequest request) {
        return ResponseEntity.ok(userRoleUpdater.updateRole(request.userId(), request.toCommand()));
    }

    @PostMapping("/refresh")
    @Override
    public ResponseEntity<JwtDto> refreshToken() {
        JwtDtoWithRefresh jwtDtoWithRefresh = tokenService.rotateRefreshToken();
        tokenService.addRefreshTokenCookie(jwtDtoWithRefresh.refreshToken());

        return ResponseEntity.ok(jwtDtoWithRefresh.jwtDto());
    }
}
