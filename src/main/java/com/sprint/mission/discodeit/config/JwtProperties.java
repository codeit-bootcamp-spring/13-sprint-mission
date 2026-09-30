package com.sprint.mission.discodeit.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties("discodeit.jwt")
public class JwtProperties {
    // signature 용 salt
    private String secret;

    private Integer refreshExpireDate;

    // 시간간격을 나타내는 자료형
    private Duration accessTokenValidate = Duration.ofMinutes(15);

    private String issuer = "discodeit";

    private Duration refreshTokenValidity = Duration.ofDays(refreshExpireDate);

}
