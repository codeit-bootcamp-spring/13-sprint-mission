package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.TokenRefreshResult;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.security.JwtRefreshService;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME =
            "REFRESH_TOKEN";

    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE =
            60 * 60 * 24 * 14;

    private final UserService userService;
    private final JwtRefreshService jwtRefreshService;

    // CSRF 토큰 발급
    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(
            CsrfToken csrfToken
    ) {

        String tokenValue =
                csrfToken.getToken();

        log.debug(
                "CSRF 토큰 요청: {}",
                tokenValue
        );

        return ResponseEntity
                .status(
                        HttpStatus.NON_AUTHORITATIVE_INFORMATION
                )
                .build();
    }

    /**
     * Refresh Token을 이용한 JWT 재발급
     */
    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String refreshToken =
                findRefreshToken(
                        request
                );

        if (refreshToken == null
                || refreshToken.isBlank()) {

            throw new BadCredentialsException(
                    "Refresh Token이 존재하지 않습니다."
            );
        }

        TokenRefreshResult refreshResult =
                jwtRefreshService.refresh(
                        refreshToken
                );

        Cookie refreshTokenCookie =
                new Cookie(
                        REFRESH_TOKEN_COOKIE_NAME,
                        refreshResult.refreshToken()
                );

        refreshTokenCookie.setHttpOnly(true);

        /*
         * 로컬 HTTP 테스트 환경에서는 false.
         *
         * 실제 HTTPS 운영 환경에서는 true 사용 권장.
         */
        refreshTokenCookie.setSecure(false);

        refreshTokenCookie.setPath("/");

        refreshTokenCookie.setMaxAge(
                REFRESH_TOKEN_COOKIE_MAX_AGE
        );

        response.addCookie(
                refreshTokenCookie
        );

        return ResponseEntity.ok(
                refreshResult.jwtDto()
        );
    }

    // 사용자 권한 수정
    @PutMapping("/role")
    public ResponseEntity<UserResponse> updateRole(
            @Valid
            @RequestBody
            UserRoleUpdateRequest request
    ) {

        UserResponse response =
                userService.updateRole(
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }

    private String findRefreshToken(
            HttpServletRequest request
    ) {

        Cookie[] cookies =
                request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if (REFRESH_TOKEN_COOKIE_NAME.equals(
                    cookie.getName()
            )) {

                return cookie.getValue();
            }
        }

        return null;
    }
}