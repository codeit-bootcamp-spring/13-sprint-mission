package com.sprint.mission.discodeit.service;

import java.time.Duration;
import java.util.Optional;

public interface RefreshTokenCookieManager {

    Optional<String> readRefreshToken();

    void writeRefreshTokenCookie(String refreshToken, Duration maxAge);

    void clearRefreshTokenCookie();
}
