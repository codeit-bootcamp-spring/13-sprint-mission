package com.sprint.mission.discodeit.security;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieProvider {

    public ResponseCookie create(String refreshToken, Duration maxAge) {
        return ResponseCookie.from("REFRESH_TOKEN", refreshToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    public ResponseCookie delete() {
        return ResponseCookie.from("REFRESH_TOKEN", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }
}
