package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import jakarta.validation.constraints.*;

public record CreateUserRequest(
        @NotBlank(message = "유저 이름은 필수입니다.")
        String username,

        @NotBlank(message = "이메알은 입력은 필수입니다.")
        String email,

        @NotBlank(message = "비밀번호 입력은 필수입니다.")
        String password
) {

    public CreateUserCommand toCommand() {
        return new CreateUserCommand(username, email, password);
    }
}
