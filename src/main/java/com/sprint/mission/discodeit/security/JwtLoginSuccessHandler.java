package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler
        implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${discodeit.jwt.refresh-token-expiration}")
    private Duration refreshTokenExpiration;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        DiscodeitUserDetails userDetails =
                (DiscodeitUserDetails) authentication.getPrincipal();

        String accessToken =
                jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken =
                jwtTokenProvider.generateRefreshToken(userDetails);

        ResponseCookie refreshTokenCookie = ResponseCookie
                .from(
                        JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                        refreshToken
                )
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();

        JwtDto jwtDto = new JwtDto(
                userDetails.getUser(),
                accessToken
        );

        log.info(
                "JWT 로그인 성공: username={}",
                userDetails.getUsername()
        );

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookie.toString()
        );

        objectMapper.writeValue(
                response.getWriter(),
                jwtDto
        );
    }
}