package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.exception.auth.InvalidRefreshTokenException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenRefresher {

    private final JwtTokenProvider jwtTokenProvider;
    private final DiscodeitUserDetailsService userDetailsService;

    public TokenPair refresh(String refreshToken) {

        if (!jwtTokenProvider.validateToken(refreshToken)
                || !jwtTokenProvider.isRefreshToken(refreshToken)) {

            throw new InvalidRefreshTokenException();
        }

        String username =
                jwtTokenProvider.getUsername(refreshToken);

        UserDetails loadedUser =
                userDetailsService.loadUserByUsername(username);

        DiscodeitUserDetails userDetails =
                (DiscodeitUserDetails) loadedUser;

        String newAccessToken =
                jwtTokenProvider.generateAccessToken(userDetails);

        String newRefreshToken =
                jwtTokenProvider.generateRefreshToken(userDetails);

        return new TokenPair(
                newAccessToken,
                newRefreshToken
        );
    }

    public record TokenPair(
            String accessToken,
            String refreshToken
    ) {
    }
}