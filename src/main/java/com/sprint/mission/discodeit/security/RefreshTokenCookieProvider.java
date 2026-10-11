package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.config.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieProvider {

    private static final String SAME_SITE = "Lax";
    private static final String PATH = "/";

    private final JwtProperties jwtProperties;

    public ResponseCookie create(
            String refreshToken,
            boolean secure
    ) {
        return ResponseCookie
                .from(
                        JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                        refreshToken
                )
                .httpOnly(true)
                .secure(secure)
                .sameSite(SAME_SITE)
                .path(PATH)
                .maxAge(jwtProperties.refreshTokenExpiration())
                .build();
    }

    public ResponseCookie delete(boolean secure) {
        return ResponseCookie
                .from(
                        JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                        ""
                )
                .httpOnly(true)
                .secure(secure)
                .sameSite(SAME_SITE)
                .path(PATH)
                .maxAge(Duration.ZERO)
                .build();
    }
}