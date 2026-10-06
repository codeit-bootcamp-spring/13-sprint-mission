package com.sprint.mission.discodeit.security;

import java.util.UUID;

public interface JwtRegistry {

    /**
     * 로그인 성공 시 JWT 정보를 등록한다.
     */
    void registerJwtInformation(
            JwtInformation jwtInformation
    );

    /**
     * 특정 사용자의 모든 JWT 정보를 무효화한다.
     */
    void invalidateJwtInformationByUserId(
            UUID userId
    );

    /**
     * 특정 사용자가 현재 로그인 상태인지 확인한다.
     */
    boolean hasActiveJwtInformationByUserId(
            UUID userId
    );

    /**
     * Access Token이 Registry에 등록되어 있는지 확인한다.
     */
    boolean hasActiveJwtInformationByAccessToken(
            String accessToken
    );

    /**
     * Refresh Token이 Registry에 등록되어 있는지 확인한다.
     */
    boolean hasActiveJwtInformationByRefreshToken(
            String refreshToken
    );

    /**
     * Refresh Token 재발급 시
     * 기존 JWT 정보를 새로운 JWT 정보로 교체한다.
     */
    void rotateJwtInformation(
            String refreshToken,
            JwtInformation newJwtInformation
    );

    /**
     * 만료된 JWT 정보를 Registry에서 제거한다.
     */
    void clearExpiredJwtInformation();
}