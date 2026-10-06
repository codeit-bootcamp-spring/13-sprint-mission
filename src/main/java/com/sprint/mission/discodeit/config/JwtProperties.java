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

    private Integer refreshExpiredDate;

    // 시간간격을 나타내는 자료형
    private Integer accessTokenValidate;

    private String issuer = "discodeit";

    public Duration getAccessTokenValidity(){
        return Duration.ofMinutes(accessTokenValidate);
    }

    public Duration getRefreshTokenValidity(){
        return Duration.ofDays(refreshExpiredDate);
    }


}
