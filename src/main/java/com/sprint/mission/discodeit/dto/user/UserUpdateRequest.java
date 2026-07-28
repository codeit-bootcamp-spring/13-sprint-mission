package com.sprint.mission.discodeit.dto.user;

import com.sprint.mission.discodeit.dto.command.user.UserUpdateCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UserUpdateRequest(
    @Schema(description = "수정할 사용자 이름", example = "홍길이")
    @Pattern(regexp = "\\S+", message = "이름은 공백일 수 없습니다.")
    String newUsername,
    @Schema(description = "수정할 이메일", example = "aaa@example.com")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String newEmail,
    @Schema(description = "수정할 비밀번호", example = "0000")
    @Pattern(regexp = "\\S+", message = "비밀번호는 공백일 수 없습니다.")
    String newPassword
)
{
    public UserUpdateCommand toCommand() {
        return new UserUpdateCommand(newUsername, newEmail, newPassword);
    }
}
