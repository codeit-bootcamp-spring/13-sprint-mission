package com.sprint.mission.discodeit.config;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "discodeit.admin.user")
public record AdminUserProperties(
        @NotBlank(message = "관리자 사용자명은 필수입니다.")
        String username,
        @NotBlank(message = "관리자 비밀번호는 필수입니다.")
        String password,
        @NotBlank(message = "관리자 이메일은 필수입니다.")
        @Email(message = "관리자 이메일 형식이 올바르지 않습니다.")
        String email
) {
}
