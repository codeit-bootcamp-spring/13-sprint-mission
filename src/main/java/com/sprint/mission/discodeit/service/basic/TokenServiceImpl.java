package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.dto.response.JwtDtoWithRefresh;
import com.sprint.mission.discodeit.dto.response.TokenDto;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;
import com.sprint.mission.discodeit.security.*;
import com.sprint.mission.discodeit.service.RefreshTokenCookieManager;
import com.sprint.mission.discodeit.service.TokenService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final DiscodeitUserDetailsService discodeitUserDetailsService;
    private final RefreshTokenCookieManager refreshTokenCookieManager;
    private final JwtRegistry jwtRegistry;

    @Override
    public JwtDtoWithRefresh rotateRefreshToken() {
        String refreshToken = getRefreshToken();
        if(!jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)){
            throw new TokenRenewalFailedException();
        }

        Claims claims = jwtTokenProvider.validateToken(refreshToken)
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

        jwtRegistry.rotateJwtInformation(refreshToken, new JwtInformation(userDto, tokenDto.accessToken(), tokenDto.refreshToken()));

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
        Duration maxAge = jwtTokenProvider.getRefreshTokenExpirationTime();
        refreshTokenCookieManager.writeRefreshTokenCookie(refreshToken, maxAge);
    }

    private boolean isTokenRefreshType(Claims claims) {
        return JwtTokenProvider.TokenType.REFRESH.name().equals(jwtTokenProvider.getTokenType(claims));
    }

    private String getRefreshToken() {
        return refreshTokenCookieManager.readRefreshToken()
                .orElseThrow(TokenRenewalFailedException::new);
    }
}
