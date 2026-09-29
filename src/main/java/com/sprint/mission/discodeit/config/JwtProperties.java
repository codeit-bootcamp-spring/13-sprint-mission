package com.sprint.mission.discodeit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "discodeit.jwt")
public class JwtProperties {

    private String secretKey;

    private Duration accessTokenValidity = Duration.ofMinutes(30);
    private String issuer = "discodeit";

    private Duration refreshTokenValidity = Duration.ofDays(14);
}
