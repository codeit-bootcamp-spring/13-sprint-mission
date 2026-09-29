package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.JwtRefreshResult;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    @Value("${discodeit.jwt.refresh-token-validity}")
    private Duration refreshTokenValidity;

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        String tokenValue = csrfToken.getToken();

        log.debug("CSRF 토큰 요청: {}", tokenValue);

        return ResponseEntity.status(HttpStatus.NON_AUTHORITATIVE_INFORMATION).build();
    }

    @PutMapping("/role")
    public ResponseEntity<UserDto> updateRole(@Valid @RequestBody UserRoleUpdateRequest request) {
        UserDto userDto = authService.updateRole(request);

        return ResponseEntity.ok().body(userDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refresh(@CookieValue(name = "REFRESH_TOKEN") String refreshToken,
                                          HttpServletResponse response) {
        JwtRefreshResult result = authService.refresh(refreshToken);

        //refresh 토큰 쿠키 설정
        ResponseCookie refreshCookie = ResponseCookie.from("REFRESH_TOKEN", result.refreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(refreshTokenValidity)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString()
        );

        return ResponseEntity.ok().body(result.jwtDto());
    }

}
