package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.config.RefreshCookieProperties;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.JwtDtoWithRefresh;
import com.sprint.mission.discodeit.dto.response.TokenDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshCookieProperties refreshCookieProperties;
    private final DiscodeitUserDetailsService discodeitUserDetailsService;

    private final ObjectProvider<HttpServletResponse> responseProvider;
    private final ObjectProvider<HttpServletRequest> requestProvider;

    public static final String REFRESH_TOKEN = "REFRESH_TOKEN";

    @Override
    public JwtDtoWithRefresh rotateRefreshToken() {
        Claims claims = jwtTokenProvider.validateToken(getRefreshToken())
                .filter(this::isTokenRefreshType)
                .orElseThrow(TokenRenewalFailedException::new);

        DiscodeitUserDetails userDetails;
        try {
            userDetails = (DiscodeitUserDetails) discodeitUserDetailsService.loadUserByUsername(claims.getSubject());
        } catch (UsernameNotFoundException e) {
            throw new TokenRenewalFailedException();
        }

        UserDto userDto = userDetails.getUserDto();
        TokenDto tokenDto = generateToken(userDto);

        return new JwtDtoWithRefresh(
                new JwtDto(userDto, tokenDto.accessToken()),
                tokenDto.refreshToken()
        );
    }

    @Override
    public TokenDto generateToken(UserDto userDto) {
        String accessToken = jwtTokenProvider.generateAccessToken(userDto.id(), userDto.username(), userDto.role());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDto.id(), userDto.username());

        return new TokenDto(accessToken, refreshToken);
    }

    @Override
    public void addRefreshTokenCookie(String refreshToken) {
        responseProvider.getObject().addHeader(HttpHeaders.SET_COOKIE, createRefreshTokenCookie(refreshToken).toString());
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN, refreshToken)
                .httpOnly(refreshCookieProperties.httpOnly())
                .secure(refreshCookieProperties.secure())
                .sameSite(refreshCookieProperties.sameSite())
                .maxAge(jwtTokenProvider.getRefreshTokenLifetime())
                .build();
    }

    private boolean isTokenRefreshType(Claims claims) {
        return JwtTokenProvider.TokenType.REFRESH.name().equals(jwtTokenProvider.getTokenType(claims));
    }

    private String getRefreshToken() {
        Cookie[] cookies = requestProvider.getObject().getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
