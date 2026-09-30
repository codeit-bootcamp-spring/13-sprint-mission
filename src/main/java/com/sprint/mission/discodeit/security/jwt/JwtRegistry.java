package com.sprint.mission.discodeit.security.jwt;

import java.util.UUID;

public interface JwtRegistry{
    void registerJwtInformation(JwtInformation info);
    void invalidateJwtInformationByUserId(UUID userId);
    boolean hasActiveJwtInformationByUserId(UUID userId);
    boolean hasActiveJwtInformationByAccessToken(String token);
    boolean hasActiveJwtInformationByRefreshToken(String token);
    void rotateJwtInformation(String refresh, JwtInformation accessInfo);
    void clearExpiredJwtInformation();
}
