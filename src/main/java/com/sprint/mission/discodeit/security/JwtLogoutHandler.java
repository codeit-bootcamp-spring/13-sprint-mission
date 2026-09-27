package com.sprint.mission.discodeit.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtLogoutHandler implements LogoutHandler {

    private static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(cookie ->
                            REFRESH_TOKEN_COOKIE.equals(cookie.getName())
                    )
                    .findFirst()
                    .ifPresent(cookie ->
                            invalidateRefreshToken(cookie.getValue())
                    );
        }

        ResponseCookie expiredCookie =
                ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .sameSite("Lax")
                        .maxAge(Duration.ZERO)
                        .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                expiredCookie.toString()
        );
    }

    private void invalidateRefreshToken(String refreshToken) {
        if (!jwtRegistry.hasActiveJwtInformationByRefreshToken(
                refreshToken
        )) {
            return;
        }

        try {
            UUID userId = jwtTokenProvider.getUserId(
                    jwtTokenProvider.parseRefreshToken(refreshToken)
            );

            jwtRegistry.invalidateJwtInformationByUserId(userId);

        } catch (IllegalArgumentException e) {
            log.warn(
                    "[JWT] 로그아웃 토큰 무효화 실패: {}",
                    e.getMessage()
            );
        }
    }
}