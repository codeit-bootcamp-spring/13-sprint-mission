package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.RefreshCookieProperties;
import com.sprint.mission.discodeit.service.RefreshTokenCookieManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenCookieManagerImpl implements RefreshTokenCookieManager {

    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";

    private final ObjectProvider<HttpServletResponse> responseProvider;
    private final ObjectProvider<HttpServletRequest> requestProvider;
    private final RefreshCookieProperties refreshCookieProperties;

    @Override
    public Optional<String> readRefreshToken() {
        Cookie[] cookies = requestProvider.getObject().getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN))
                .findFirst()
                .map(Cookie::getValue)
                .filter(token -> !token.isBlank());
    }

    @Override
    public void writeRefreshTokenCookie(String refreshToken, Duration maxAge) {
        ResponseCookie cookie = createRefreshTokenCookie(refreshToken, maxAge);
        responseProvider.getObject().addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Override
    public void clearRefreshTokenCookie() {
        writeRefreshTokenCookie("", Duration.ZERO);
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken, Duration maxAge) {
        // 발급·삭제 모두 Path와 Domain을 생략해 /api/auth 하위 요청의 동일한 쿠키 범위를 사용한다.
        return ResponseCookie.from(REFRESH_TOKEN, refreshToken)
                .httpOnly(refreshCookieProperties.httpOnly())
                .secure(refreshCookieProperties.secure())
                .sameSite(refreshCookieProperties.sameSite())
                .maxAge(maxAge)
                .build();
    }
}
