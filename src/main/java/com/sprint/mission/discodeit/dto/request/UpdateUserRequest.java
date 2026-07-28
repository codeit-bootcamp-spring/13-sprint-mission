package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.*;

public record UpdateUserRequest(
        @Size(max = 30, message = "사용자 이름은 30자 이하여야 합니다.")
        String username,

        @Email(message = "올바른 이메일 형식이 아닙니다.")
        @Size(max = 50, message = "이메일은 50자 이하여야 합니다.")
        String email,

        @Size(min = 5, max = 20, message = "비밀번호는 5자 이상 20자 이하여야 합니다.")
        String password,

        @Schema(description = "프로필 이미지 파일")
        MultipartFile profileImage
) {
    public UpdateUserCommand toCommand() {
        return new UpdateUserCommand(username, email, password, profileImage);
    }
}
