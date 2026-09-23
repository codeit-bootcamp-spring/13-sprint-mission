package com.sprint.mission.discodeit.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "discodeit.security.remember-me")
public record RememberMeProperties(
        @NotBlank(message = "Remember-Me 서명 키는 필수입니다.")
        String key,
        @Positive(message = "Remember-Me 토큰 유효기간은 양수여야 합니다.")
        int tokenValiditySeconds
) {
}
