package com.sprint.mission.discodeit.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;

public record UserRequest(
        @NotBlank(
                groups = Create.class,
                message = "사용자 이름은 필수입니다."
        )
        @Size(min = 2, max = 50, message = "사용자 이름은 2자 이상 50자 이하여야 합니다.")
        String username,

        @NotBlank(
                groups = Create.class,
                message = "이메일은 필수입니다."
        )
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        String email,

        @NotBlank(
                groups = Create.class,
                message = "비밀번호는 필수입니다."
        )
        @Size(min = 8, max = 100, message = "비밀번호는 8자 이상 100자 이하여야 합니다.")
        String password
) {

    public interface Create extends Default {
    }
}