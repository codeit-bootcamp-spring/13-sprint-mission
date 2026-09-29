package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserResponse;
import com.sprint.mission.discodeit.exception.auth.InvalidRefreshTokenException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final DiscodeitUserDetailsService userDetailsService;

    @Value("${discodeit.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        String tokenValue = csrfToken.getToken();

        log.debug("CSRF 토큰 요청: {}", tokenValue);

        return ResponseEntity
                .status(HttpStatus.NON_AUTHORITATIVE_INFORMATION)
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refresh(
            @CookieValue(
                    name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                    required = false
            )
            String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (refreshToken == null
                || !jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        String username = jwtTokenProvider.getUsername(refreshToken);

        DiscodeitUserDetails userDetails;

        try {
            userDetails = (DiscodeitUserDetails)
                    userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException exception) {
            throw new InvalidRefreshTokenException();
        }

        String newAccessToken =
                jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken =
                jwtTokenProvider.generateRefreshToken(userDetails);

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from(
                        JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                        newRefreshToken
                )
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookie.toString()
        );

        JwtDto jwtDto = new JwtDto(
                userDetails.getUser(),
                newAccessToken
        );

        return ResponseEntity.ok(jwtDto);
    }

    @PutMapping("/role")
    public ResponseEntity<UserResponse> updateRole(
            @Valid
            @RequestBody UserRoleUpdateRequest request
    ) {
        UserResponse response = authService.updateRole(request);

        return ResponseEntity.ok(response);
    }
}