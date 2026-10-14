package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.CacheNames;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.entity.Role;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    @Value("${discodeit.jwt.refresh-token-validity}")
    private Duration refreshTokenValidity;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();

        String username = userDetails.getUsername();
        Role role = userDetails.getUserDto().role();

        String accessToken = jwtTokenProvider.createAccessToken(username, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(username, role);

        // 생성한 access, refresh 토큰으로 jwtInformation 생성 및 registry에 등록(로그인 성공 시 JWT 정보 등록)
        JwtInformation jwtInformation = new JwtInformation(userDetails.getUserDto(), accessToken, refreshToken);
        jwtRegistry.registerJwtInformation(jwtInformation);

        // 유저 목록 캐시 무효화
        Cache usersCache = cacheManager.getCache(CacheNames.USERS);
        if (usersCache != null) {
            usersCache.clear();
        }

        ResponseCookie refreshCookie = refreshTokenCookieProvider.create(refreshToken, refreshTokenValidity);

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        JwtDto jwtDto = new JwtDto(userDetails.getUserDto(), accessToken);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), jwtDto);
    }
}
