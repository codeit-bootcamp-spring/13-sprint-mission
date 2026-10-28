package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.exception.jwt.TokenRenewalFailedException;

import java.util.Collection;
import java.util.Set;
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

    Set<UUID> findActiveUserIds(Collection<UUID> userIds);

    /**
     * 등록된 세션에 유효한 Access Token이 하나라도 있는 사용자 ID를 조회한다.
     * 만료되거나 로그아웃한 세션은 온라인 판정에서 제외한다.
     */
    Set<UUID> findUserIdsWithValidAccessToken(Collection<UUID> userIds);

    /**
     * 유효한 액세스 토큰이 없는지 재확인하고 온라인 추적 항목을 제거한다.
     * 실제로 제거한 호출만 true를 반환하여 여러 모니터의 중복 상태 발행을 줄인다.
     * 등록·갱신과 경쟁할 때의 보호 방식은 각 저장소 구현체가 담당한다.
     */
    boolean tryRemoveOfflineTracking(UUID userId);
}
