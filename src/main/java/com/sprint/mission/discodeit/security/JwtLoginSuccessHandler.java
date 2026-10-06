package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.UserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler
        implements AuthenticationSuccessHandler {

    private static final String REFRESH_TOKEN_COOKIE_NAME =
            "REFRESH_TOKEN";

    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE =
            60 * 60 * 24 * 14;

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        DiscodeitUserDetails userDetails =
                (DiscodeitUserDetails)
                        authentication.getPrincipal();

        UserResponse userResponse =
                userDetails.getUserDto();

        String accessToken =
                jwtTokenProvider
                        .generateAccessToken(
                                userResponse.getId(),
                                userResponse.getUsername(),
                                userResponse.getRole()
                        );

        String refreshToken =
                jwtTokenProvider
                        .generateRefreshToken(
                                userResponse.getId(),
                                userResponse.getUsername(),
                                userResponse.getRole()
                        );

        /*
         * 로그인 성공 시
         * Access Token / Refresh Token 정보를 Registry에 등록한다.
         *
         * InMemoryJwtRegistry에서 maxActiveJwtCount = 1로 관리하므로
         * 동일 계정으로 다시 로그인하면 기존 JwtInformation이 제거되고
         * 새로운 JwtInformation만 유지된다.
         */
        JwtInformation jwtInformation =
                new JwtInformation(
                        userResponse,
                        accessToken,
                        refreshToken
                );

        jwtRegistry.registerJwtInformation(
                jwtInformation
        );

        Cookie refreshTokenCookie =
                new Cookie(
                        REFRESH_TOKEN_COOKIE_NAME,
                        refreshToken
                );

        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(false);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(
                REFRESH_TOKEN_COOKIE_MAX_AGE
        );

        response.addCookie(
                refreshTokenCookie
        );

        JwtDto jwtDto =
                new JwtDto(
                        userResponse,
                        accessToken
                );

        response.setStatus(
                HttpServletResponse.SC_OK
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        objectMapper.writeValue(
                response.getWriter(),
                jwtDto
        );
    }
}