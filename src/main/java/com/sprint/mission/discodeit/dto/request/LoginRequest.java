package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.constraints.*;

@Schema(description = "로그인 정보")
public record LoginRequest(
        @NotBlank(message = "이메일 입력은 필수 입니다,")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호 입력은 팔수 입니다.")
        @Size(min = 5, max = 20, message = "비밀번호는 5자 이상 20자 이하여야 합니다.")
        String password) {

    public LoginCommand toCommand() {
        return new LoginCommand(email(), password());
    }

}
