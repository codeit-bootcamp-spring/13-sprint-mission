package com.sprint.mission.discodeit.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private static final String REFRESH_TOKEN_COOKIE_NAME =
            "REFRESH_TOKEN";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {

        Cookie[] cookies =
                request.getCookies();

        if (cookies != null) {

            Arrays.stream(cookies)
                    .filter(cookie ->
                            REFRESH_TOKEN_COOKIE_NAME.equals(
                                    cookie.getName()
                            )
                    )
                    .findFirst()
                    .ifPresent(cookie -> {

                        String refreshToken =
                                cookie.getValue();

                        if (
                                refreshToken != null
                                        && !refreshToken.isBlank()
                                        && jwtTokenProvider
                                        .validateRefreshToken(
                                                refreshToken
                                        )
                                        && jwtRegistry
                                        .hasActiveJwtInformationByRefreshToken(
                                                refreshToken
                                        )
                        ) {

                            UUID userId =
                                    jwtTokenProvider
                                            .getUserId(
                                                    refreshToken
                                            );

                            jwtRegistry
                                    .invalidateJwtInformationByUserId(
                                            userId
                                    );
                        }
                    });
        }

        Cookie deletedRefreshTokenCookie =
                new Cookie(
                        REFRESH_TOKEN_COOKIE_NAME,
                        null
                );

        deletedRefreshTokenCookie.setHttpOnly(true);
        deletedRefreshTokenCookie.setSecure(false);
        deletedRefreshTokenCookie.setPath("/");
        deletedRefreshTokenCookie.setMaxAge(0);

        response.addCookie(
                deletedRefreshTokenCookie
        );
    }
}