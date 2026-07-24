package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.dto.command.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.*;

public record UpdateUserRequest(
        @NotBlank(message = "유저 이름은 필수입니다.")
        String username,

        @NotBlank(message = "이메알은 입력은 필수입니다.")
        String email,

        @NotBlank(message = "비밀번호 입력은 필수입니다.")
        String password,

        @Schema(description = "프로필 이미지 파일")
        MultipartFile profileImage
) {
    public UpdateUserCommand toCommand() {
        return new UpdateUserCommand(username, email, password, profileImage);
    }
}
