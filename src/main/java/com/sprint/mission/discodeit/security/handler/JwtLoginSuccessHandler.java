package com.sprint.mission.discodeit.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.config.RefreshCookieProperties;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String REFRESH_TOKEN = "REFRESH_TOKEN";

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final RefreshCookieProperties refreshCookieProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        DiscodeitUserDetails discodeitUser = (DiscodeitUserDetails) authentication.getPrincipal();
        UserDto body = discodeitUser.getUserDto();

        String accessToken = jwtTokenProvider.generateAccessToken(body.id(), body.username(), body.role());
        String refreshToken = jwtTokenProvider.generateRefreshToken(body.id(), body.username());

        JwtDto jwtDto = new JwtDto(body, accessToken);

        addRefreshTokenToResponse(response, refreshToken);
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), jwtDto);
    }

    private void addRefreshTokenToResponse(HttpServletResponse response, String refreshToken) {
        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN, refreshToken);
        refreshTokenCookie.setHttpOnly(refreshCookieProperties.httpOnly());
        refreshTokenCookie.setSecure(refreshCookieProperties.secure());
        refreshTokenCookie.setAttribute("SameSite", refreshCookieProperties.sameSite());
        refreshTokenCookie.setMaxAge((int) jwtTokenProvider.getRefreshTokenLifetime());
        response.addCookie(refreshTokenCookie);
    }
}
