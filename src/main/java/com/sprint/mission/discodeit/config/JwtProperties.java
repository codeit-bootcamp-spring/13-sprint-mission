package com.sprint.mission.discodeit.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discodeit.jwt")
public record JwtProperties(
    String issuer,
    String secret,
    Duration accessTokenExpiration,
    Duration refreshTokenExpiration
) {

}
