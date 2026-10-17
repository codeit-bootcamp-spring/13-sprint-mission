package com.sprint.mission.discodeit.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final CacheManager cacheManager;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

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
                            RefreshTokenCookieProvider.COOKIE_NAME
                                    .equals(cookie.getName())
                    )
                    .findFirst()
                    .ifPresent(cookie ->
                            invalidateRefreshToken(cookie.getValue())
                    );
        }

        ResponseCookie expiredCookie =
                refreshTokenCookieProvider.expire();

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

            Cache usersCache = cacheManager.getCache("users");

            if (usersCache != null) {
                usersCache.clear();
            }

        } catch (IllegalArgumentException e) {
            log.warn(
                    "[JWT] 로그아웃 토큰 무효화 실패: {}",
                    e.getMessage()
            );
        }
    }
}