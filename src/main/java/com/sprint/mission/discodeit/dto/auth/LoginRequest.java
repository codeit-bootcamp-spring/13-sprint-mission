package com.sprint.mission.discodeit.dto.auth;

import com.sprint.mission.discodeit.dto.command.auth.LoginCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "이름은 필수 입니다.")
        String username,
        @Schema(description = "비밀번호", example = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "비밀번호는 필수 입니다.")
        String password
)
{
    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }
}
