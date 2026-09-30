package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;

import java.util.UUID;

public interface JwtRegistry {
    void registerJwtInformation(JwtInformation jwtInformation);

    void invalidateJwtInformationByUserId(UUID userId);

    /**
     * 해당 사용자의 리프레시 토큰이 일치하는 경우에만 토큰 쌍을 원자적으로 삭제한다.
     * 사용자나 토큰이 등록되어 있지 않으면 아무것도 변경하지 않는다.
     */
    void invalidateJwtInformationByRefreshToken(UUID userId, String refreshToken);

    boolean hasActiveJwtInformationByUserId(UUID userId);

    boolean hasActiveJwtInformationByAccessToken(String accessToken);

    boolean hasActiveJwtInformationByRefreshToken(String refreshToken);

    /**
     * 등록된 리프레시 토큰을 새 토큰 정보로 원자적으로 교체한다.
     *
     * @throws TokenRenewalFailedException 사용자가 등록되어 있지 않거나 기존 토큰이 일치하지 않는 경우
     */
    void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation);

    void clearExpiredJwtInformation();
}
