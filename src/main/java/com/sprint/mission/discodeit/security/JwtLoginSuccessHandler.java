package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;
    private final RefreshTokenCookieProvider refreshTokenCookieProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication
    ) throws IOException, ServletException {

        DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();

        UserDto userDto = userDetails.getUserDto();

        String accessToken = jwtTokenProvider.createAccessToken(
                userDto.id(),
                userDto.username(),
                userDto.role()
        );

        String refreshToken = jwtTokenProvider.createRefreshToken(
                userDto.id(),
                userDto.username()
        );

        JwtInformation jwtInformation = new JwtInformation(
                userDto,
                accessToken,
                refreshToken
        );

        jwtRegistry.registerJwtInformation(jwtInformation);

        Cache usersCache = cacheManager.getCache("users");

        if (usersCache != null) {
            usersCache.clear();
        }

        ResponseCookie refreshTokenCookie =
                refreshTokenCookieProvider.create(refreshToken);

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookie.toString()
        );

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        JwtDto jwtDto = new JwtDto(
                userDto,
                accessToken
        );

        objectMapper.writeValue(
                response.getWriter(),
                jwtDto
        );

    }
}
