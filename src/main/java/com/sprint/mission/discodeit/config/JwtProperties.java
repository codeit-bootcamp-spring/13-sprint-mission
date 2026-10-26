package com.sprint.mission.discodeit.config;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "discodeit.jwt")
public record JwtProperties(
        @NotBlank(message = "JWT secret은 필수입니다.")
        String secret,

        @NotNull(message = "Access Token 만료 시간은 필수입니다.")
        Duration accessTokenExpiration,

        @NotNull(message = "Refresh Token 만료 시간은 필수입니다.")
        Duration refreshTokenExpiration
) {

    @AssertTrue(
            message = "JWT secret은 32바이트 이상이어야 합니다."
    )
    public boolean isSecretLengthValid() {
        return secret != null
                && secret.getBytes(StandardCharsets.UTF_8).length
                >= 32;
    }
}