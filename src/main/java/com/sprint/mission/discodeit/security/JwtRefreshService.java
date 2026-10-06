package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.TokenRefreshResult;
import com.sprint.mission.discodeit.dto.UserResponse;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtRefreshService {

    private final JwtTokenProvider jwtTokenProvider;

    private final JwtRegistry jwtRegistry;

    private final UserService userService;

    public TokenRefreshResult refresh(
            String refreshToken
    ) {

        /*
         * 1. Refresh Token 기본 검증
         * 2. Registry에 활성 상태로 등록된 Refresh Token인지 검증
         */
        if (
                refreshToken == null
                        || refreshToken.isBlank()
                        || !jwtTokenProvider
                        .validateRefreshToken(
                                refreshToken
                        )
                        || !jwtRegistry
                        .hasActiveJwtInformationByRefreshToken(
                                refreshToken
                        )
        ) {

            throw new BadCredentialsException(
                    "유효하지 않은 Refresh Token입니다."
            );
        }

        UUID userId =
                jwtTokenProvider
                        .getUserId(
                                refreshToken
                        );

        /*
         * JWT 내부 정보만 그대로 사용하는 것이 아니라
         * DB에서 현재 사용자 정보를 다시 조회한다.
         *
         * 이를 통해 최신 Role 등의 사용자 정보를
         * 새 토큰에 반영할 수 있다.
         */
        UserResponse userResponse =
                userService.read(
                        userId
                );

        String newAccessToken =
                jwtTokenProvider
                        .generateAccessToken(
                                userResponse.getId(),
                                userResponse.getUsername(),
                                userResponse.getRole()
                        );

        String newRefreshToken =
                jwtTokenProvider
                        .generateRefreshToken(
                                userResponse.getId(),
                                userResponse.getUsername(),
                                userResponse.getRole()
                        );

        /*
         * 새로운 JWT 정보를 생성한다.
         */
        JwtInformation newJwtInformation =
                new JwtInformation(
                        userResponse,
                        newAccessToken,
                        newRefreshToken
                );

        /*
         * Refresh Token Rotation
         *
         * Registry에 저장된 기존 Refresh Token 정보를 제거하고
         * 새 Access Token / Refresh Token 정보로 교체한다.
         */
        jwtRegistry.rotateJwtInformation(
                refreshToken,
                newJwtInformation
        );

        JwtDto jwtDto =
                new JwtDto(
                        userResponse,
                        newAccessToken
                );

        return new TokenRefreshResult(
                jwtDto,
                newRefreshToken
        );
    }
}