package com.sprint.mission.discodeit.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtLogoutHandler implements LogoutHandler {

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        ResponseCookie deletedRefreshTokenCookie = ResponseCookie
                .from(
                        JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                        ""
                )
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                deletedRefreshTokenCookie.toString()
        );
    }
}