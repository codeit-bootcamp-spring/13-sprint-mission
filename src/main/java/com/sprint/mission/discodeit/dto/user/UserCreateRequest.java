package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.command.user.UserCreateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @Schema(description = "사용자 이름", examples = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "이름을 입력해주세요.")
        String username,
        @Schema(description = "이메일", examples = "hong@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,
        @Schema(description = "비밀번호", examples = "1234", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "비밀번호를 입력해주세요.")
        String password
)
{
    public UserCreateCommand toCommand(){
        return new UserCreateCommand(username, email, password);
    }
}


