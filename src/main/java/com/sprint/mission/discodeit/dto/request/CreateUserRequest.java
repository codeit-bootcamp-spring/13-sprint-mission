package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank(message = "사용자 이름은 필수입니다.")
        @Size(max = 30, message = "사용자 이름은 30자 이하여야 합니다.")
        String username,

        @NotBlank(message = "이메일 입력은 필수입니다.")
        @Email(message = "올바른 이메일의 형식이 아닙니다.")
        @Size(max = 50, message = "이메일은 50자 이하여야 합니다.")
        String email,

        @NotBlank(message = "비밀번호 입력은 필수입니다.")
        @Size(min = 5, max = 20, message = "비밀번호는 5자 이상 20자 이하여야 합니다.")
        String password
) {

    public CreateUserCommand toCommand() {
        return new CreateUserCommand(username, email, password);
    }
}
