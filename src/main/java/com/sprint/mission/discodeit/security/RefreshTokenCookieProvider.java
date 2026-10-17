package com.sprint.mission.discodeit.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieProvider {

    public static final String COOKIE_NAME = "REFRESH_TOKEN";

    private final JwtTokenProvider jwtTokenProvider;

    public ResponseCookie create(String refreshToken) {
        return ResponseCookie
                .from(COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(
                        Duration.ofSeconds(
                                jwtTokenProvider
                                        .getRefreshTokenExpirationTime()
                        )
                )
                .build();
    }

    public ResponseCookie expire() {
        return ResponseCookie
                .from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
    }
}