package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.config.CacheNames;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtRegistry jwtRegistry;
    private final CacheManager cacheManager;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    @Override
    public void logout(HttpServletRequest request,
                       HttpServletResponse response,
                       Authentication authentication) {

        Arrays.stream(Optional.ofNullable(request.getCookies())
                        .orElse(new Cookie[0]))
                .filter(cookie -> "REFRESH_TOKEN".equals(cookie.getName()))
                .findFirst()
                .ifPresent(cookie -> {
                    // 서버 쪽에서 무효화
                    jwtRegistry.invalidateJwtInformationByRefreshToken(cookie.getValue());

                    // 유저 목록 캐시 무효화
                    Cache usersCache = cacheManager.getCache(CacheNames.USERS);
                    if (usersCache != null) {
                        usersCache.clear();
                    }

                    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieProvider.delete().toString());
                });
    }
}
