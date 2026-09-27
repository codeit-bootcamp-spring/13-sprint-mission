package com.sprint.mission.discodeit.security.handler;

import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.RefreshTokenCookieManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {
    private final RefreshTokenCookieManager refreshTokenCookieManager;
    private final JwtRegistry jwtRegistry;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        refreshTokenCookieManager.readRefreshToken().ifPresent(this::invalidateRefreshToken);
        refreshTokenCookieManager.clearRefreshTokenCookie();
    }

    private void invalidateRefreshToken(String refreshToken) {
        jwtTokenProvider.validateToken(refreshToken)
                .filter(claims -> JwtTokenProvider.TokenType.REFRESH.name().equals(jwtTokenProvider.getTokenType(claims)))
                .ifPresent(claims -> jwtRegistry.invalidateJwtInformationByRefreshToken(
                        jwtTokenProvider.getUserId(claims), refreshToken));
    }
}
