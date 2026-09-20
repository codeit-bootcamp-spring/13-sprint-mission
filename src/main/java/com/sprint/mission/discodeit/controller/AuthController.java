package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.exception.auth.InvalidRefreshTokenException;
import com.sprint.mission.discodeit.security.jwt.JwtTokenRefresher;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME =
            "REFRESH_TOKEN";

    private final JwtTokenRefresher jwtTokenRefresher;

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshToken(request);

        if (refreshToken == null) {
            throw new InvalidRefreshTokenException();
        }

        JwtTokenRefresher.TokenPair tokenPair =
                jwtTokenRefresher.refresh(refreshToken);

        Cookie refreshTokenCookie = new Cookie(
                REFRESH_TOKEN_COOKIE_NAME,
                tokenPair.refreshToken()
        );

        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok(
                new JwtDto(tokenPair.accessToken())
        );
    }

    private String extractRefreshToken(
            HttpServletRequest request
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie ->
                        REFRESH_TOKEN_COOKIE_NAME.equals(
                                cookie.getName()
                        )
                )
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}